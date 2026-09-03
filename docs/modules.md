# Modules

## auth

Mints JWTs. `POST /auth/login` takes `{username, password}`, posts them to the
user service's `/users/authenticate` over `WebClient`, and returns `{token}`.
Bad credentials come back as 401.

`JwtService` signs RS256 tokens carrying username (`sub`), the account UUID
(`userId`) and roles, valid for 30 minutes. This is the only place in the
project that loads the private key.

## user

Owns `ArenaUserDocument` (userId, username, password, email, role) in MongoDB
and implements `ReactiveUserDetailsService`, so Spring Security authenticates
against it.

`SecurityConfiguration` is the fullest security setup in the project: CSRF off,
CORS limited to `http://localhost:3000`, BCrypt encoding, and route rules —
`/user/register` and `/users/authenticate` public, `/user/**` for `USER`,
`/admin/**` for `ADMIN`, Swagger and `/develop/**` for `DEVELOPER`. A
`JwtAuthenticationFilter` populates the reactive security context from a bearer
token. This module's `JwtService` verifies only; it cannot issue a token.

**Queueing** is explicit rather than a side effect of logging in: being signed
in is presence, asking for a match is intent. `/user/queue/join` checks the
character against your roster (a call to the character service with your own
token) before publishing `USER_CONNECTED`. Both queue endpoints answer with how
many subscribers received the event, so a zero tells you matchmaking is not
listening.

## character

Characters, abilities, and combat resolution. Reactive throughout; database
`character-db`. Every endpoint needs a bearer token — the module is a JWT
resource server verifying with the public key, so it does not depend on a
gateway to know who is calling.

```
model/          Character (abstract) + 13 subclasses, Ability, CombatResult, TargetOutcome
model/enums/    CharacterClass, PowerResourceType, ArmorType, StatType, AbilityType,
                TargetType, Role, Specialization + the 13 per-class specialization enums
dto/            CreateCharacterRequest, UpdateCharacterRequest
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

### Creating a character

```
POST /api/characters -> CurrentUserProvider.currentUser    (userId from the JWT)
                     -> CharacterService.createCharacter
                     -> RosterPolicy.checkHasRoom          (character.roster.max-size, default 10)
                     -> CharacterFactoryRegistry.create    (by CharacterClass)
                     -> XxxFactory.create                  (knows the concrete type)
                     -> setOwnerId, then save
```

Spring injects every `CharacterFactory` into the registry, which indexes them by
`supports()` and fails at startup if a `CharacterClass` has no factory or two
claim the same one.

**Adding a class** means: the model, its specialization enum, a constant in
`CharacterClass`, and one `CharacterFactory`. Nothing else changes.

An unknown specialization is rejected with 400 listing the valid values, inside
`Mono.fromCallable`, so nothing is persisted.

### Where the stats live

Each specialization enum implements `Specialization` and carries its role and
base stats:

```java
ARMS(Role.DAMAGE, Map.of(StatType.STRENGTH, 100)),
FURY(Role.DAMAGE, Map.of(StatType.STRENGTH, 120)),
PROTECTION(Role.TANK, Map.of(StatType.STRENGTH, 80));
```

Constructors read from that map, so a class's numbers live in one place. Stats a
class does not use are absent and resolve to 0 through `getStatValue(StatType)`,
which is how ability scaling works without knowing the concrete subclass.

Armor follows from the class: `setCharacterClass` derives the `ArmorType`
(plate/mail/leather/cloth) and its base value.

### Resolving a cast

`POST /api/abilities/cast` with `{casterId, abilityId, targetIds}`:

1. Load caster and ability. `SELF` abilities ignore the supplied targets;
   every other type uses the given ids. Ally/enemy and turn state are out of
   scope here.
2. Run `CastRuleChain`:

   | Order | Rule                   | Fails with                                                 |
   |-------|------------------------|------------------------------------------------------------|
   | 10    | `CasterOwnershipRule`  | 403 — the caster is not on your account                     |
   | 20    | `ResourceTypeRule`     | 400 — the ability costs a resource this class does not use   |
   | 30    | `ResourceCostRule`     | 400 — cannot afford it                                       |
   | 40    | `TargetsRequiredRule`  | 400 — no targets given                                       |

   Ownership first, so probing someone else's character returns 403 without
   leaking its resource state. Nothing is written until every rule passes.
3. Spend the resource. Effect amount is
   `basePower + (casterStat × scalingFactor)`, rounded.
4. Per target, `AbilityEffectRegistry` resolves the effect and applies it. Each
   target is saved.
5. Return `CombatResult` with a `TargetOutcome` per target.

`DamageEffect` mitigates with armor: `armor / (armor + 400)`, capped at 75%,
floor of 1 damage. Plate (200) mitigates ~33%, cloth (50) ~11%. `HealEffect` is
unmitigated and clamped at max health.

`BUFF` and `DEBUFF` are valid types with no effect yet: such a cast spends the
resource, reports 0 and moves no health. Implementing them means adding an
`AbilityEffect` bean, not editing the pipeline.

### Notes

- All characters share the `characters` collection; Spring Data writes a
  `_class` discriminator so subclasses read back as their concrete type.
- The `dev` profile wipes and reseeds both collections. Seed characters are
  spread across one dev account per class (`dev-warrior`, `dev-priest`, …),
  which keeps every roster under the limit and gives you several accounts to
  test ownership against.

## matchmaking

Pairs waiting players. Driven by Redis pub/sub, with one read endpoint on top.

`RedisSubscriberService` subscribes to `user.status.*` once the application is
ready and retries with backoff if Redis is unavailable. `MatchmakingService`
keeps an in-memory queue of `(userId, characterId)`: `USER_CONNECTED` enqueues,
`USER_DISCONNECTED` dequeues, and two waiting players are paired and published
on `match.found`.

Players queue with the character they intend to play, so a match carries both
sides' character and a session is playable the moment it is created.

`GET /api/matchmaking/me` answers whether you are queued, how many are waiting,
and who you were last matched with — for the caller only, from the `userId`
claim, never a path parameter.

Two guards: joining twice is ignored, so a client cannot be paired with itself;
and entries older than `matchmaking.queue.entry-ttl-seconds` (default 120) are
dropped before each match attempt, because a player who closes the tab never
sends `USER_DISCONNECTED`.

The whole Redis wiring sits behind `redis.enabled` (default true) — set it false
and the context starts with no Redis, which is what the tests do.

Queue and recent-match state are in memory, so a restart empties both and the
service cannot be scaled past one instance. The last 1000 matches are kept.

The module's `Dockerfile` runs the boot jar on `eclipse-temurin:25-jre-alpine`
as a non-root user, copying `build/libs/*-SNAPSHOT.jar` — the boot jar, not the
`-plain.jar` beside it.

## game

Consumes `match.found` and owns what happens next. Sessions live in MongoDB
(`game-db`) and survive a restart.

`MatchFoundSubscriber` turns each event into a `GameSession` with both
participants, the first-queued player on turn, and status `IN_PROGRESS`.

### A turn

```
POST /api/games/{id}/cast -> is the game in progress?      else 400
                          -> are you a player?             else 403
                          -> is it your turn?              else 403
                          -> is your turn still live?      else 400
                          -> character service POST /api/abilities/cast
                             (your token, your character, opponent as target)
                          -> record the outcome, pass the turn, new deadline
                          -> opponent defeated? finish and set the winner
```

Casting out of turn is refused before the character service is called, so no
resource is spent. The target is always the opponent's character; `SELF`
abilities are resolved by the character service.

### Ending a game

Recorded as `endReason`:

- `DEFEAT` — the opponent's character reached 0 health.
- `SURRENDER` — `POST /{id}/surrender`, allowed on either player's turn.
- `TIMEOUT` — each turn carries a `turnDeadline` (`game.turn.timeout-seconds`,
  default 120). Once it passes the waiting player calls
  `POST /{id}/claim-timeout` and takes the win; the player who ran out cannot
  cast any more.

Timeouts are claimed rather than swept by a background job: nothing has to be
scheduled, the service stays stateless, and a game only ends when someone is
actually waiting on it.

`POST /{id}/rematch` starts a fresh game between the same two players once the
first has finished, with the loser moving first. One rematch per game.

### Arenas

A session records an `arenaMapId` (from `game.arena.map-id`), and each
participant carries a `position` and a `movementRemaining`.

Deployment is deferred rather than done at match time, and the reason is
structural: a session is created from a `match.found` Redis event, which carries
no user token, while every read of the map service needs one. So the first
request from either player — a move or a cast — asks the map service for
`deployments`, places both players and saves. `game` never learns what terrain
is: it asks map where players can start and what a route costs, and map answers
in coordinates.

`POST /{id}/move` charges the A* path cost against the turn's movement budget
(`game.arena.movement-per-turn`, default 4). Moving does not end the turn, so a
player may walk and then cast. A move is refused if the destination is
unreachable, costs more than the budget left, or has the opponent standing on
it. The budget is restored when a turn passes to a player.

`cast` measures the hex distance between the two players and sends it to the
character service, where `RangeRule` compares it against the ability's `range`.
Leaving `game.arena.map-id` blank turns all of this off: positions stay null,
the distance is sent as null, and `RangeRule` skips — the game plays exactly as
it did before arenas existed.

## map

Hexagonal arenas: their terrain, their tiles, and the cost of crossing them.

### Coordinates

Tiles are addressed by cube coordinates, `HexCoordinate(q, r, s)`, which is a
record whose compact constructor rejects anything that does not satisfy
`q + r + s = 0`. An invalid coordinate therefore cannot be constructed, stored
or deserialised — the check does not have to be repeated at each call site.
`HexCoordinate.axial(q, r)` derives the third axis; `key()` and `parse()` move
between a coordinate and the `q:r:s` string used as the Mongo map key.

`HexDirection` holds the six cube offsets once, and `neighbours()` is derived
from it, so there is a single definition of what "adjacent" means.

### Terrain

`TerrainType` carries its own rules — whether it can be entered and what
entering it costs — the same enum-as-data shape the character module uses for
class stats:

| Terrain    | Passable | Movement cost |
|------------|----------|---------------|
| `PLAIN`    | yes      | 1             |
| `FOREST`   | yes      | 2             |
| `HILLS`    | yes      | 2             |
| `DESERT`   | yes      | 3             |
| `WATER`    | no       | —             |
| `MOUNTAIN` | no       | —             |

A `HexTile` is a coordinate, a terrain and an elevation; it answers `passable()`
and `movementCost()` by delegating to its terrain rather than storing a second,
divergent copy of them.

### Authoring

Maps are drawn by hand, not generated. An arena is a JSON document — a legend
and a grid of rows — so the file still looks like the map it describes and can
be kept under version control:

```json
{
  "name": "Frozen Pass",
  "description": "A narrow icy corridor",
  "radius": 2,
  "legend": { ".": "PLAIN", "f": "FOREST", "^": "MOUNTAIN", "~": "WATER" },
  "grid": [
    "~ . .",
    ". f . ^",
    ". . . f .",
    ". ^ . .",
    ". . ~"
  ]
}
```

Rows run from `r = -radius` to `r = +radius` and hold `2*radius+1-|r|` cells
each, which is why the grid is widest in the middle. Every cell is one legend
character and whitespace inside a row is decoration, so `"~ . ."` and `"~.."`
are the same row — align it however reads best. A row with the wrong number of
cells is rejected by row number, axial coordinate and both counts
(`grid row 2 (r=-1) has 5 cells, expected 4`). An optional `elevations` block of
the same shape carries heights.

`ArenaFormat` converts between that document and tiles.
`POST /api/maps/import` creates a map from one, `GET /{id}/grid` gives it back
and `PUT /{id}/grid` redraws an existing map in place. Because each
`TerrainType` carries a canonical symbol and the renderer emits only the terrain
actually used, an exported arena is stable: it round-trips through an editor
without churning in git.

`MapGenerator.fill(map, radius, TileFactory.uniform(terrain))` still exists, but
only to lay down a flat canvas of `3r² + 3r + 1` tiles to start drawing on.

### Pathfinding

`HexPathfinder.shortestPath` is A* over `movementCost`, with hex distance as the
heuristic — admissible because no step costs less than 1, so the first path it
finds is the cheapest one. Impassable tiles and tiles outside the map are never
entered. An unreachable goal returns an empty path rather than an error, and the
endpoint reports it as `reachable: false`.

`pathCost` charges for every tile *entered*, so the starting tile is free.

`DeploymentPlanner` picks starting positions: the passable tile furthest from
the centre, then repeatedly whichever passable tile is furthest from everything
already chosen. `GET /{id}/deployments?count=2` is what the game service calls,
and it means no other module needs to know what `TerrainType` is.

### Ownership

A map carries an indexed `ownerId` taken from the token, never from the request
body. Reads are open to any authenticated caller — maps are a shared library —
but every write is owner-only and answers `403` otherwise. `MapProperties` caps
both the radius of a generated map and how many maps one account may own, the
same shape as the character module's `RosterPolicy`.
