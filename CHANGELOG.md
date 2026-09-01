# Changelog

All notable changes to this project are recorded here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project aims to follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Nothing has been released yet: everything below is under `0.0.1-SNAPSHOT`.
Entries before this file existed were reconstructed from the working tree, so
this log starts at the point it was introduced rather than at the first commit.

## [Unreleased]

### Added

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
- Documentation: `docs/architecture.md`, `docs/modules.md`, `docs/api.md`, and
  this changelog.

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
