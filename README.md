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
| `auth`        | Login, issues JWTs                        | Working                  | none          | 8080  |
| `user`        | Accounts, registration, credential check  | Working                  | MongoDB       | 8081  |
| `matchmaking` | Pairs waiting players                     | Working, unconnected     | Redis         | 8082  |
| `character`   | Characters, abilities, combat resolution  | Most complete            | MongoDB       | 8083  |
| `map`         | Arenas / maps                             | Empty placeholder        | none          | 8080  |

## Identity and ownership

An account owns characters, and you may only play as your own.

```
POST /user/register              create the account            (user, public)
POST /auth/login                 -> auth calls user's
                                    POST /users/authenticate    (service to service)
                                 <- JWT { sub: username,
                                          userId: <uuid>,
                                          roles: [...] }
Authorization: Bearer <jwt>      -> character validates the same token itself
                                    and reads userId as the owner
```

- `userId` is a UUID generated at registration and stored as the account's
  `_id`. It is the ownership key everywhere, so renaming an account never
  orphans its characters.
- Every `Character` carries an indexed `ownerId`. It is set from the token on
  create and is never read from the request body.
- Tokens are RS256. `auth` signs with a private key; `user` and `character`
  hold only the public key, so they can verify a token but cannot mint one -
  a service that is compromised cannot forge an identity.
- Keys are PEM resources: `ttarena.jwt.private-key` (auth only) and
  `ttarena.jwt.public-key` (everywhere), overridable with
  `TTARENA_JWT_PRIVATE_KEY` / `TTARENA_JWT_PUBLIC_KEY`. A development pair is
  committed under `src/main/resources/keys/` so the stack runs out of the box -
  it is not secret; see `auth/src/main/resources/keys/README.md` to replace it.

What ownership gates:

| Action                          | Rule                                        |
|---------------------------------|---------------------------------------------|
| Create a character              | Stamped with your `userId`, roster limit applies |
| Update / delete a character     | Yours only, enforced by the query           |
| Cast an ability **as** a caster | Yours only, else 403                        |
| Target another character        | Anyone - you must be able to attack opponents |
| Read a character                | Any authenticated user - you must see opponents |

---

## auth

Issues JWTs. `POST /auth/login` takes `{username, password}`, posts them to the
user service's `/users/authenticate` over `WebClient`, and on success returns
`{token}`. Bad credentials come back as 401. The user service's address is
`user-service.base-url` (default `http://localhost:8081`).

`JwtService` signs RS256 tokens carrying username (`sub`), the account UUID
(`userId`) and roles, valid for 30 minutes. It is the only place in the project
that loads the private key. `AuthenticatedUserPrincipal` is what
carries the UUID from the user service's response into the token - without it
the `userId` claim would just repeat the username.

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

`ArenaUserPrincipal` adapts a stored account into Spring Security's
`UserDetails` while keeping the UUID, which is what lets the token carry a
stable owner id. This module's `JwtService` verifies tokens only - it has no
private key and no way to issue one.

Endpoints:

| Method | Path                  | Auth   | Notes                              |
|--------|-----------------------|--------|------------------------------------|
| POST   | `/user/register`      | public | 201, BCrypt, unique username, 3-32 alphanumeric |
| POST   | `/users/authenticate` | public | service-to-service, used by `auth` |
| GET    | `/user/home`          | USER   |                                    |

`RedisPublisherService` publishes user status events as JSON on
`user.status.<userId>` - see Event flow below. It is wired but still nothing
calls it.

## character

Owns characters and their abilities, and resolves a single ability cast.
Reactive stack throughout (WebFlux + reactive MongoDB), database
`character-db` on `localhost:27017`. Every endpoint requires a valid bearer
token: the module is a JWT resource server verifying `ttarena.jwt.secret`
itself, so it does not depend on a gateway to know who is calling.

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
security/       CurrentUser, CurrentUserProvider (reads the JWT claims)
factory/        CharacterFactory + 13 implementations + CharacterFactoryRegistry
combat/         AbilityEffect + DamageEffect, HealEffect + AbilityEffectRegistry
                CastRule + 4 rules + CastRuleChain, CastContext
service/        CharacterService, AbilityService, RosterPolicy
controller/     CharacterController, AbilityController
repository/     CharacterRepository, AbilityRepository (reactive Mongo)
config/         MongoConfig, SecurityConfiguration,
                DataInitializer, AbilityDataInitializer (both @Profile("dev"))
exception/      GlobalExceptionHandler + BadRequest / NotFound / Forbidden / Database
```

### How character creation works

One endpoint creates every class. `CreateCharacterRequest` names the class as
an enum and the specialization as a string; `CharacterFactoryRegistry` looks up
the `CharacterFactory` registered for that class and delegates to it. The
factory parses the string into its own specialization enum (case-insensitive)
and calls the constructor.

```
POST /api/characters -> CurrentUserProvider.currentUser    (userId from the JWT)
                     -> CharacterService.createCharacter
                     -> RosterPolicy.checkHasRoom          (max 10 per account)
                     -> CharacterFactoryRegistry.create    (by CharacterClass)
                     -> XxxFactory.create                  (knows the concrete type)
                     -> setOwnerId, then save
```

Character names must be letters only (`^\p{L}+$`, max 32) and account usernames
alphanumeric (`^[\p{L}\p{N}]+$`, 3-32). Both use Unicode classes, so `Conán` and
`Ñuria` are fine while digits, spaces and punctuation are not.

The rules are enforced twice on purpose: as bean validation on the request
records, which turns a bad value into a 400 with a readable message, and inside
the domain, so that a seeder, a migration or any future internal caller cannot
write a name the API would have rejected. In the domain every write path is
covered - the constructor, the setter and (for the account) Lombok's builder,
which sets fields directly and would otherwise be a way around the check.

Reads are deliberately not validated: `name` and `username` are mapped with
`@AccessType(FIELD)`, so Spring Data writes the field directly when loading a
document instead of calling the validating setter. A hand-edited or legacy
document still loads and can be fixed, rather than making the record
unreadable. Source files are compiled as UTF-8 (set explicitly in each
build file) since these patterns are exercised with accented literals.

The owner is taken from the token, never from the body, so a client cannot
create a character on someone else's account. `character.roster.max-size`
(default 10) caps how many an account may hold.

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

1. Load caster and ability. `SELF` abilities ignore the supplied targets and
   target the caster; every other target type uses the supplied ids. Ally/enemy
   validation and turn state are out of scope here.
2. Run `CastRuleChain` - ordered `CastRule` beans, every one a separate class:

   | Order | Rule                  | Fails with |
   |-------|-----------------------|------------|
   | 10    | `CasterOwnershipRule` | 403 - the caster is not on your account |
   | 20    | `ResourceTypeRule`    | 400 - the ability costs a resource this class does not use |
   | 30    | `ResourceCostRule`    | 400 - cannot afford it |
   | 40    | `TargetsRequiredRule` | 400 - no targets given |

   Ownership is checked first, so probing someone else's character returns 403
   without leaking its resource state. Nothing is written until every rule
   passes. Adding a precondition means adding a `CastRule` bean.
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
| GET    | `/me`                     | your roster only                       |
| GET    | `/{id}`                   | 404 if unknown                         |
| GET    | `/name/{name}`            | 404 if unknown                         |
| GET    | `/class/{characterClass}` | e.g. `/class/WARRIOR`                  |
| POST   | `/`                       | creates any class, 201                 |
| PUT    | `/{id}`                   | rename only, yours only, 404 otherwise |
| DELETE | `/{id}`                   | yours only, 404 otherwise, 204 on success |

All of these need `Authorization: Bearer <jwt>`.

```json
POST /api/characters
{
  "name": "Conan",
  "characterClass": "WARRIOR",
  "health": 200,
  "resourceAmount": 100,
  "specialization": "ARMS"
}

PUT /api/characters/{id}
{
  "name": "Kanan"
}
```

`PUT` takes an `UpdateCharacterRequest` and changes the name, nothing else. It
used to accept a whole `Character`, which could never work - `Character` is
abstract, so Jackson had no way to choose a subclass - and which would have let
a client rewrite its own health, resource and owner. Health and resources are
combat state; they move through `/api/abilities/cast`, not through an update.

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
  (`DataInitializer`, `AbilityDataInitializer`). Seed characters are spread
  across one dev account per class (`dev-warrior`, `dev-priest`, ...), which
  keeps every roster under the limit and gives you several accounts to test
  ownership against.
- Tests cover the factory registry over every class, the specialization stat
  data, the mitigation and healing arithmetic, the cast pipeline against mocked
  repositories, the roster limit, and ownership (including that a rejected cast
  spends nothing and that attacking another account's character is allowed). No database required. The controllers have no slice
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

The module's `Dockerfile` runs the boot jar on `eclipse-temurin:25-jre-alpine`
as a non-root user. It copies `build/libs/*-SNAPSHOT.jar`, which matches the
boot jar and not the `-plain.jar` sitting beside it.

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

- Nothing publishes user status events, and nothing consumes `match.found`, so
  matchmaking never sees a player.
- The development keypair is committed. Replace it before anything is exposed,
  and treat the old HS256 secret in the git history as burned.
- Password reset, email verification and token refresh do not exist.
- Every module except `character` has only a `contextLoads()` test.
- `matchmaking` is the only module with a Dockerfile.
