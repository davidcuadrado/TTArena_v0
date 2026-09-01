# TTArena

Tech Stack:

- Front: Phaser.js (2D) or Three.js(3D) // Decision pending
- Back: Spring Boot
- DB: MongoDB + MySQL // Redis?
- WebSockets (Spring WebFlux)
- Redis Pub/Sub
- Docker
- APISIX

Build: `./gradlew clean build` — Gradle 9.7.1, Java 25 toolchain, Spring Boot 4.1.1.
Every module is an independent Spring Boot application (its own `build.gradle.kts`
and boot jar). The root project is a container only.

## Modules at a glance

| Module        | Purpose                                   | State                    | Storage       | Port  |
|---------------|-------------------------------------------|--------------------------|---------------|-------|
| `auth`        | Login, issues JWTs                        | Partial                  | none          | 8080  |
| `user`        | User accounts, authentication backing     | Partial                  | MongoDB       | 8080  |
| `character`   | Characters, abilities, combat resolution  | Most complete            | MongoDB       | 8080  |
| `matchmaking` | Pairs waiting players                     | Working, unconnected     | Redis         | 8082  |
| `map`         | Arenas / maps                             | Empty placeholder        | none          | 8080  |

Only `matchmaking` sets a port, so the others cannot run simultaneously as-is.

---

## auth

Issues JWTs. `POST /auth/login` takes `{username, password}`, calls the user
service over `WebClient` to look the user up, runs the credentials through a
`ReactiveAuthenticationManager`, and returns `{token}` on success.

`JwtService` signs HS256 tokens carrying username, user id and roles, valid for
30 minutes, and can validate a token and extract those claims back out.
`JwtAuthenticationFilter` reads the `Authorization: Bearer` header for incoming
requests.

Two things are unfinished here:

- The login flow posts to `http://user-service/users/authenticate`. That
  hostname assumes service discovery that is not configured, and the `user`
  module does not currently expose that endpoint — so login cannot succeed yet.
- The signing secret is a hardcoded constant in `JwtService`, duplicated
  verbatim in the `user` module. It belongs in configuration, injected per
  environment, before anything is deployed.

## user

Owns `ArenaUserDocument` (id, username, password, email, role) in MongoDB, and
implements `ReactiveUserDetailsService` so Spring Security can authenticate
against it. `ArenaUserService.findByUsername` maps a stored user into Spring's
`UserDetails` with roles split from the `role` field.

`SecurityConfiguration` is the only real security setup in the project: CSRF
off, CORS limited to `http://localhost:3000`, BCrypt password encoding, and
route rules — `/authenticate/**` and `/register/**` public, `/user/**` and
`/character/**` for `USER`, `/admin/**` for `ADMIN`, Swagger and `/develop/**`
for `DEVELOPER`. A `JwtAuthenticationFilter` sits at the authentication step and
populates the reactive security context from a bearer token.

`RedisPublisherService` publishes user status events as JSON on
`user.status.<userId>` — see Event flow below.

Gaps: the module has no MongoDB URI configured (so it falls back to the driver
default), `UserController` exposes only `/user/home`, and there is no
registration or authentication endpoint yet — which is what `auth` is trying to
call. `RedisPublisherService` is wired but nothing calls it.

## character

Owns characters and their abilities, and resolves a single ability cast.
Reactive stack throughout (WebFlux + reactive MongoDB), database
`character-db` on `localhost:27017`.

### What it does

- CRUD over characters. Thirteen classes, each with its own specializations,
  power resource (rage, mana, energy, ...), armor type and stats.
- A catalogue of abilities, queryable by class and specialization.
- Combat resolution for one cast: spend the caster's resource, scale the
  effect off the caster's stat, apply it to each target, persist everyone
  touched, and return a summary.

### Layout

```
model/          Character (abstract) + 13 subclasses, Ability, CombatResult, TargetOutcome
model/enums/    CharacterClass, PowerResourceType, ArmorType, StatType, AbilityType,
                TargetType, Role, Specialization + the 13 per-class specialization enums
dto/            CreateCharacterRequest
factory/        CharacterFactory + 13 implementations + CharacterFactoryRegistry
combat/         AbilityEffect + DamageEffect, HealEffect + AbilityEffectRegistry
service/        CharacterService, AbilityService
controller/     CharacterController, AbilityController
repository/     CharacterRepository, AbilityRepository (reactive Mongo)
config/         MongoConfig, DataInitializer, AbilityDataInitializer (both @Profile("dev"))
exception/      GlobalExceptionHandler + BadRequest / NotFound / Database (all unchecked)
```

### How character creation works

One endpoint creates every class. `CreateCharacterRequest` names the class as
an enum and the specialization as a string; `CharacterFactoryRegistry` looks up
the `CharacterFactory` registered for that class and delegates to it. The
factory parses the string into its own specialization enum (case-insensitive)
and calls the constructor.

```
POST /api/characters -> CharacterService.createCharacter
                     -> CharacterFactoryRegistry.create   (by CharacterClass)
                     -> XxxFactory.create                 (knows the concrete type)
                     -> CharacterRepository.save
```

Spring injects every `CharacterFactory` bean into the registry, which indexes
them by `supports()`. The registry checks its own completeness at construction:
a `CharacterClass` with no factory, or two factories claiming the same class,
fails the application context at startup.

**Adding a class** means: the model, its specialization enum, a constant in
`CharacterClass`, and one `CharacterFactory` implementation. No changes to the
service, the controller or any existing factory.

An unknown specialization is rejected with 400 and a message listing the valid
values for that class. Rejection happens inside `Mono.fromCallable`, so nothing
is persisted.

### Where the stats live

Each specialization enum implements `Specialization` and carries its own
`Role` and base stats:

```java
ARMS(Role.DAMAGE, Map.of(StatType.STRENGTH, 100)),
FURY(Role.DAMAGE, Map.of(StatType.STRENGTH, 120)),
PROTECTION(Role.TANK, Map.of(StatType.STRENGTH, 80));
```

Constructors read from that map, so a class's numbers live in one place next to
the role they belong with. Stats a class does not use are simply absent and
resolve to 0 via `getStatValue(StatType)`, which is how ability scaling works
without knowing the concrete subclass.

Armor follows from the class: `Character.setCharacterClass` derives the
`ArmorType` (plate/mail/leather/cloth) and its base armor value.

### How a cast resolves

`POST /api/abilities/cast` with `{casterId, abilityId, targetIds}`:

1. Load caster and ability. Reject if the ability's resource type is not the
   caster's, or the caster cannot afford it (400).
2. `SELF` abilities ignore the supplied targets and target the caster.
   Every other target type uses the supplied ids; ally/enemy validation and
   turn state are out of scope here.
3. Spend the resource. Effect amount is
   `basePower + (casterStat * scalingFactor)`, rounded.
4. Per target, `AbilityEffectRegistry` resolves the `AbilityEffect` for the
   ability's type and applies it. Each target is saved.
5. Return `CombatResult` with a `TargetOutcome` per target (amount applied,
   resulting health, whether it died).

`DamageEffect` mitigates with armor: `armor / (armor + 400)`, capped at 75%,
with a floor of 1 damage per hit. Plate (200) mitigates ~33%, cloth (50) ~11%.
`HealEffect` is unmitigated and clamped at max health by `Character`.

`BUFF` and `DEBUFF` are valid ability types with no effect implemented yet.
The registry deliberately allows that gap: such a cast still spends the
resource, reports an amount of 0, and moves no health. Implementing them means
adding an `AbilityEffect` bean, not editing the cast pipeline.

### API

Characters, `/api/characters`:

| Method | Path                      | Notes                                  |
|--------|---------------------------|----------------------------------------|
| GET    | `/`                       | all characters                         |
| GET    | `/{id}`                   | 404 if unknown                         |
| GET    | `/name/{name}`            | 404 if unknown                         |
| GET    | `/class/{characterClass}` | e.g. `/class/WARRIOR`                  |
| POST   | `/`                       | creates any class, 201                 |
| PUT    | `/{id}`                   | replaces the document                  |
| DELETE | `/{id}`                   | 204                                    |

```json
POST /api/characters
{
  "name": "Conan",
  "characterClass": "WARRIOR",
  "health": 200,
  "resourceAmount": 100,
  "specialization": "ARMS"
}
```

Abilities, `/api/abilities`:

| Method | Path                                                         |
|--------|--------------------------------------------------------------|
| GET    | `/`                                                          |
| GET    | `/{id}`                                                      |
| GET    | `/class/{characterClass}`                                    |
| GET    | `/class/{characterClass}/specialization/{specialization}`    |
| POST   | `/cast`                                                      |

### Notes

- All characters share the `characters` collection; Spring Data writes a
  `_class` discriminator so subclasses read back as their concrete type.
- The `dev` profile wipes and reseeds both collections at startup
  (`DataInitializer`, `AbilityDataInitializer`).
- Tests cover the factory registry over every class, the specialization stat
  data, the mitigation and healing arithmetic, and the cast pipeline against
  mocked repositories. No database required. The controllers have no slice
  test yet: `spring-boot-starter-security` is on the classpath with no
  security configuration in this module, so a `@WebFluxTest` would hit
  default authentication.

## matchmaking

Runs on port 8082. No HTTP API — it is driven entirely by Redis pub/sub.

`RedisSubscriberService` subscribes to `user.status.*` once the application is
ready, and retries with backoff if Redis is unavailable. `MatchmakingService`
keeps an in-memory `ConcurrentLinkedQueue` of waiting user ids: `USER_CONNECTED`
enqueues, `USER_DISCONNECTED` dequeues, and as soon as two players are waiting
they are paired and a `MATCH_FOUND` event is published on `match.found` by
`MatchFoundPublisher`.

The whole Redis wiring — template, listener container, publisher and subscriber
— is behind the `redis.enabled` property (default true). Setting it to false
lets the context start with no Redis at all, which is what the tests use.

Because the queue is in memory, matchmaking state does not survive a restart
and the service cannot be scaled to more than one instance as it stands.

## map

A generated Spring Boot skeleton — an application class and a context test,
nothing else. Arenas and map data are not implemented.

## Event flow

```
user  --(user.status.<userId>)-->  matchmaking  --(match.found)-->  (no subscriber yet)
       RedisEvent {type,               queue of waiting               MatchFoundEvent
        userId, timestamp}             user ids                       {type, players[], timestamp}
```

Both sides use string serializers and JSON payloads, so the publisher's
`RedisEvent` and the subscriber's `RedisEvent` must stay compatible.

Neither end of this chain is connected yet: nothing in `user` calls
`RedisPublisherService.publishUserEvent`, and nothing subscribes to
`match.found`. The path has never been exercised end to end.

## Known gaps

- JWT signing secret hardcoded and duplicated in `auth` and `user`.
- `auth` login targets a `user-service` host and a `/users/authenticate`
  endpoint that do not exist yet.
- No user registration endpoint.
- Nothing publishes user status events, and nothing consumes `match.found`.
- Every module except `character` has only a `contextLoads()` test.
- `matchmaking/Dockerfile` builds on `eclipse-temurin:17`, while the project
  now targets Java 25.
- Only `matchmaking` sets a server port.
