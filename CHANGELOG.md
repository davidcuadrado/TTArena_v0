# Changelog

All notable changes to this project are recorded here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Nothing has been released yet: everything below is under `0.0.1-SNAPSHOT`.
Entries before this file existed were reconstructed from the working tree, so
this log starts at the point it was introduced rather than at the first commit.

## [Unreleased]

### Added

- **Optimistic locking on every MongoDB document.** `Character`, `Ability`,
  `ArenaUserDocument`, `GameSession` and `GameMap` carry a `@Version`, so two
  concurrent writes no longer silently lose one. A conflict surfaces as `409`.
- **RFC 7807 problem responses in `user`, `game`, `map` and `character`**, the
  shape `auth` already returned, so a client parses one error format across the
  whole system. Adds handlers for validation failures (with the offending
  fields), malformed request bodies, and version conflicts.
- **Hand-authored arenas.** An arena is a JSON document — a legend plus a grid
  of rows — so a map is a file you draw, review and keep in git.
  - `POST /api/maps/import`, `GET /api/maps/{id}/grid`, `PUT /api/maps/{id}/grid`
  - Each `TerrainType` carries a canonical symbol and the renderer emits only
    the terrain actually used, so an export round-trips without churning.
  - Whitespace inside a row is decoration: `"~ . ."` and `"~.."` are the same
    row. A miscounted row is rejected by row number, axial coordinate and both
    counts.
  - Optional `elevations` rows, bounded to 0..100.
  - `map/src/main/resources/arenas/frozen-pass.json` is a worked example.
- **Arenas wired into play.** A game session records an `arenaMapId` and each
  participant a hex position and a per-turn movement budget.
  - `POST /api/games/{id}/move` spends A* path cost from that budget; moving
    does not end the turn.
  - `Ability` gains a `range`, and a `RangeRule` joins the existing cast chain,
    so the distance the game service measures is enforced where every other
    cast rule already lives.
  - `GET /api/maps/{id}/starting-positions` returns passable, well-separated
    positions, so `game` needs no copy of `TerrainType`.
  - Players are deployed on their first action rather than at match time,
    because a `match.found` event carries no user token.
  - Leaving `game.arena.map-id` blank keeps the previous positionless combat.
- **`map` module** (port 8085, `map-db`) — ported from the pre-Boot-4 line
  (`archive/pre-boot4`) and rebuilt on the current stack.
  - `HexCoordinate` is an immutable record whose compact constructor enforces
    `q + r + s = 0`, so an invalid coordinate cannot be constructed, stored or
    deserialised; `HexDirection` defines the six neighbours once.
  - `TerrainType` carries passability and movement cost as enum data, and
    `HexTile` derives both from its terrain rather than storing a second copy.
  - `TileFactory` strategy for generation (`uniform` / `random`) with an
    injected `RandomGenerator`, so generated maps are reproducible under test.
  - `HexPathfinder` is a real A* over movement cost — the version on the old
    branch documented A* but returned `[start, goal]`.
  - `/api/maps`, `/api/maps/me`, `/api/maps/generate`,
    `/api/maps/{id}/tiles/{q}/{r}/{s}`, `/api/maps/{id}/path`
  - Maps carry an indexed `ownerId` taken from the token; reads are open to any
    authenticated caller, every write is owner-only.
  - `MapProperties` caps generated radius and maps per account.
- **Docker Compose stack** — MongoDB, Redis, all six services and an APISIX
  gateway in standalone mode, with a `Dockerfile` per module (only
  `matchmaking` had one).
- **`game` module** (port 8084, `game-db`). Consumes `match.found` and owns
  sessions: participants, turn order, and how a match ended.
  - `GET /api/games/me`, `GET /api/games`, `GET /api/games/{id}`
  - `POST /api/games/{id}/cast` — plays one turn, proxying to the character
    service with the player's own token
  - `POST /api/games/{id}/surrender`, `/claim-timeout`, `/rematch`
  - Turn deadlines (`game.turn.timeout-seconds`, default 120), claimed rather
    than swept by a scheduler
- **Ownership model.** `Character` carries an indexed `ownerId`, set from the
  token and never from the request body. Owner-scoped repository queries
  (`findByIdAndOwnerId`, `findByOwnerId`, `countByOwnerId`).
- **`RosterPolicy`** — at most `character.roster.max-size` (default 10)
  characters per account.
- **`CastRule` chain** in `character` — `CasterOwnershipRule`,
  `ResourceTypeRule`, `ResourceCostRule`, `TargetsRequiredRule`, ordered and
  applied before a cast resolves.
- **`AbilityEffect` strategy** — `DamageEffect` and `HealEffect` behind
  `AbilityEffectRegistry`; BUFF and DEBUFF resolve to no effect rather than
  failing.
- **`CharacterFactory` + registry** — one factory per class, indexed by
  `supports()`, with a startup completeness check.
- **Registration and authentication** in `user`: `POST /user/register` and
  `POST /users/authenticate` (the endpoint `auth` had always been calling).
- **Matchmaking queue endpoints**: `POST /user/queue/join` (with the character
  to play, checked against the caller's roster) and `/user/queue/leave`.
- **`GET /api/matchmaking/me`** — queue state and last match, for the caller.
- **Queue entry TTL** (`matchmaking.queue.entry-ttl-seconds`, default 120), so a
  player who vanishes is not matched against.
- **Name rules**: character names letters-only, usernames alphanumeric, both
  Unicode-aware, enforced at the API boundary *and* in the domain.
- Tests: ~60 across `character`, `matchmaking` and `game`, all without a
  database.
- **`test` profile** for `character`, `game` and `user`
  (`src/test/resources/application-test.yml`, activated by the Gradle
  `Test` task). Disables `spring.data.mongodb.auto-index-creation`, which
  removed the `MongoClientException: Shutdown in progress` stack traces
  logged at ERROR during test-context teardown.
- Documentation: `docs/architecture.md`, `docs/modules.md`, `docs/api.md`, and
  this changelog.

### Removed

- **Procedural map generation.** `TileFactory.random` and the random branch of
  `/api/maps/generate` are gone: maps are made by hand. `generate` now requires
  a terrain and lays down a flat canvas to author on top of. This also retired
  the shared `RandomGenerator` and the thread-safety problem it carried.

### Changed

- **Upgraded** Spring Boot 3.4.3 → 4.1.1, Gradle 8.10 → 9.7.1, Java 23 → 25,
  springdoc 1.8.0 → 3.1.0, jjwt 0.12.6 → 0.13.0.
  - Jackson 2 → 3 (`com.fasterxml.jackson` → `tools.jackson`)
  - `Jackson2JsonRedisSerializer` → `JacksonJsonRedisSerializer`
  - Boot 4 starter renames (`oauth2-client` → `security-oauth2-client`, etc.)
  - Dropped `io.spring.dependency-management` for Gradle's `platform()`
- **JWT is now RS256 with asymmetric keys.** `auth` signs with a private key;
  every other service verifies with the public key and cannot mint tokens.
  `user`'s `generateToken` was removed.
- `PUT /api/characters/{id}` takes an `UpdateCharacterRequest` (name only)
  instead of a whole `Character`.
- The 13 per-class create endpoints collapsed into one `POST /api/characters`.
- Specialization stats moved from `switch` blocks in every constructor onto the
  specialization enums.
- `character` is WebFlux-only; the servlet starter was removed.
- Ports assigned: auth 8080, user 8081, matchmaking 8082, character 8083,
  game 8084.
- `matchmaking/Dockerfile` runs on `eclipse-temurin:25-jre-alpine` as a
  non-root user.
- Root project is a container only — no sources, no Boot plugin.
- `README.md` reorganised as an entry point, with detail moved into `docs/`.

### Fixed

- The blanket `@ExceptionHandler(Exception.class)` in `user` and `character`
  turned every unmapped failure into a `500`, including routing errors that
  should have been `404`. Removed: unmapped failures fall through to Spring
  Boot's own handling, which keeps their real status.
- **Eager assembly in reactive chains.** `Mono.then(x)` and
  `Mono.switchIfEmpty(x)` build `x` when the chain is assembled, not when it is
  subscribed — so the argument must be free to construct.
  - `map`'s owner-quota check queried MongoDB at assembly time, which meant an
    oversized radius still cost a round-trip before being refused. It is now
    deferred, and cheap validation runs before anything touches the database.
  - 15 `switchIfEmpty(Mono.error(new ...))` sites across `user`, `game`, `map`
    and `character` allocated an exception, and filled in its stack trace, on
    every call including the ones that succeeded. All are now
    `switchIfEmpty(Mono.defer(() -> Mono.error(...)))`.
  - The same applied to the three `timeout(duration, fallback)` fallbacks in
    `game`'s service clients, which built an `UpstreamUnavailableException` on
    every upstream call rather than only on a timeout.
- `GET /api/maps` and `/api/maps/me` returned every map with every tile — tens
  of megabytes from one call. They now return `MapSummary`, a closed projection,
  so the tiles are neither read from MongoDB nor serialised. `GameMap` carries a
  stored `tileCount` maintained by the tile methods.
- `placeTile` accepted coordinates outside the map's own radius, so a map could
  declare one size and hold tiles at another. An arena's radius is now set when
  it is created (`POST /api/maps` takes one) and enforced on every placement.
- `game` called the character and map services with no response timeout, so a
  hung upstream left a player's turn hanging while its deadline ran down. Both
  clients now time out (`*-service.response-timeout`, default 5s) and answer
  `503` rather than never completing.
- `map` parsed, rendered and filled whole arenas on the Netty event loop.
  Importing, exporting, redrawing and generating now run on the parallel
  scheduler, as pathfinding and starting positions already did.
- `map`'s A* ran on the Netty event loop; it now runs on the parallel
  scheduler, which matters now that `game` calls it on every move.
- `matchmaking` read its Redis host from a hard-coded `localhost`, which cannot
  work inside Compose; it now honours `REDIS_HOST` / `REDIS_PORT`.
- `map/build.gradle.kts` declared both the reactive and the servlet web
  starters, which started the servlet stack; the servlet starter is gone.
- `matchmaking` context could not start: Redis beans were required
  unconditionally while the test excluded the auto-configuration, and the
  `spring.redis.enabled` switch was reading a property that did not exist.
- `user`'s Redis publisher could not be injected (generic type mismatch) and
  published a payload the subscriber could not read.
- A `@Configuration` class named `ReactiveRedisTemplate` collided with its own
  `@Bean` method name, failing the context.
- `DataInitializer` used `Flux.just(...)` on cold `Mono`s, so the `dev` profile
  wiped the collections and seeded nothing.
- `application.yml` in `user`, `auth` and `map` held properties syntax, so the
  application names were never applied.
- Joining the matchmaking queue twice could pair a player with themselves.
- The eager `@PostConstruct` Redis subscription and a `@Bean` method declared
  inside a `@Service` were moved to their proper lifecycle and configuration.
- `ObjectMapper` instances without `JavaTimeModule` would have failed on every
  event carrying an `Instant`.
- Source encoding pinned to UTF-8 in every module.

### Security

- The hardcoded HS256 signing secret, duplicated in two modules, was removed.
  **Treat it as compromised — it remains in the git history.**
- Development keypair added under `src/main/resources/keys/`. Not secret;
  replace before exposing anything.
- Casting is gated by ownership, and the game module cannot cast on a player's
  behalf: the player's own token is forwarded and re-checked downstream.
