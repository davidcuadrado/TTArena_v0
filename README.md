# TTArena

A turn-based arena game backend: players register, build characters from
thirteen classes, queue for a match, and fight one turn at a time.

Six independent Spring Boot services, reactive throughout (WebFlux + reactive
MongoDB), talking to each other over HTTP and Redis pub/sub, published through
an APISIX gateway.

## Requirements

- **JDK 25** — or none, and let Gradle download one (the foojay resolver is
  configured in `settings.gradle.kts`)
- **MongoDB** on `localhost:27017`
- **Redis** on `localhost:6379`
- **Docker** — only if you want the whole stack at once

```bash
docker run -d -p 27017:27017 --name ttarena-mongo mongo:8.0
docker run -d -p 6379:6379   --name ttarena-redis redis:7.4-alpine
```

## Quick start

There are two ways to run it: Compose brings the whole stack up behind the
gateway, Gradle runs the services one at a time against a local MongoDB and
Redis.

### The whole stack, with Compose

Each `Dockerfile` copies a jar Gradle has already built, so the build comes
first — `docker compose build` on a clean checkout fails on a missing jar:

```bash
./gradlew clean build        # writes <module>/build/libs/*-SNAPSHOT.jar
docker compose up --build    # mongo, redis, the six services, APISIX
```

Everything is then reachable through the gateway on `localhost:9080`, and every
service is also published on its own port for debugging. `docker compose down -v`
takes the stack down and drops its volumes.

Compose reads two variables from your environment:

```bash
TTARENA_CORS_ORIGINS=http://localhost:3000   # passed to auth and user; the default
GAME_ARENA_MAP_ID=                           # blank means positionless combat
```

### One service at a time, from source

```bash
./gradlew clean build                       # compile + test everything
./gradlew :user:bootRun
./gradlew :auth:bootRun
./gradlew :character:bootRun --args='--spring.profiles.active=dev'
./gradlew :matchmaking:bootRun
./gradlew :game:bootRun
./gradlew :map:bootRun
```

The `dev` profile on `character` wipes and reseeds its collections with sample
characters (`DataInitializer`) and abilities (`AbilityDataInitializer`).

Then walk the loop — full commands in [docs/api.md](docs/api.md):

```
POST /user/register        -> POST /auth/login   -> POST /api/characters
     :8081                      :8080                  :8083
                     |
                     v
POST /user/queue/join {characterId}   (both players)
                     |
                     v
GET /api/games/me     -> POST /api/games/{id}/cast {abilityId}
     :8084                  :8084
```

## Services

| Module        | Purpose                                  | Port | Base paths                          | Storage       |
|---------------|------------------------------------------|------|-------------------------------------|---------------|
| `auth`        | Login, mints JWTs                        | 8080 | `/auth`                             | none          |
| `user`        | Accounts, registration, queue entry      | 8081 | `/user`, `/users`                   | MongoDB       |
| `matchmaking` | Pairs waiting players                    | 8082 | `/api/matchmaking`                  | Redis         |
| `character`   | Characters, abilities, combat resolution | 8083 | `/api/characters`, `/api/abilities` | MongoDB       |
| `game`        | Match sessions and turn order            | 8084 | `/api/games`                        | MongoDB+Redis |
| `map`         | Hex arenas, terrain and pathfinding      | 8085 | `/api/maps`                         | MongoDB       |

Every module is its own Spring Boot application with its own
`build.gradle.kts`, `Dockerfile` and boot jar. The root project is a container
only: no sources, no Boot plugin, just the aggregate `clean` / `build` / `check`
tasks from the `base` plugin.

Each service keeps its own database — `user-db`, `character-db`, `game-db`,
`map-db` — and no service reads another's.

### The gateway

APISIX 3.11 runs as a standalone data plane on port **9080**: routes come from
`apisix/apisix.yaml` on disk, so there is no etcd and no Admin API to secure.
It routes and handles CORS, and deliberately does **not** verify JWTs — every
service is an OAuth2 resource server that checks the RS256 signature itself, and
a second check here would only be a second place to get it wrong.

| Gateway path                            | Upstream           |
|-----------------------------------------|--------------------|
| `/auth/*`                               | `auth:8080`        |
| `/user/*`, `/users/*`                   | `user:8081`        |
| `/api/matchmaking/*`                    | `matchmaking:8082` |
| `/api/characters/*`, `/api/abilities/*` | `character:8083`   |
| `/api/games/*`                          | `game:8084`        |
| `/api/maps/*`                           | `map:8085`         |

## Repository layout

```
auth/ user/ matchmaking/ character/ game/ map/   one Spring Boot app each
  src/main/resources/keys/                       dev public key (private in auth only)
  Dockerfile                                     temurin 25-jre-alpine, non-root
apisix/                                          config.yaml + declarative routes
map/src/main/resources/arenas/                   hand-authored arenas (frozen-pass.json)
docs/                                            architecture, modules, api
docker-compose.yml                               the whole stack
```

## How it fits together

```
POST /user/queue/join {characterId}
      |
      v
user  --(user.status.<userId>)-->  matchmaking  --(match.found)-->  game
       RedisEvent {type, userId,    pairs two waiting               creates a GameSession
        characterId, timestamp}     players                         MatchFoundEvent {type,
                                            |                        participants[{userId,
                                            v                        characterId}], timestamp}
                                  GET /api/matchmaking/me                    |
                                                                             v
                                                            POST /api/games/{id}/cast
                                                                   -> character service
```

An account owns characters and may only play as its own. Identity travels as an
RS256 JWT carrying `userId`; `auth` holds the private key, everyone else
verifies with the public one.

## Playing on an arena

Games are positionless until `game.arena.map-id` names a map. With one set,
players are deployed on first action, `POST /api/games/{id}/move` spends a
movement budget on A* path cost, and ability range is checked against the
distance between the two.

An arena is a JSON document — a legend plus a grid of rows — so a map is a file
you draw, review and keep in git. One worked example ships with the map service:

```bash
# import it (any authenticated account can); the response carries the new id
curl -X POST localhost:8085/api/maps/import -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d @map/src/main/resources/arenas/frozen-pass.json

# point the game service at it
GAME_ARENA_MAP_ID=<map-id> ./gradlew :game:bootRun
```

`GET /api/maps/{id}/grid` gives the document back, ready to edit, and
`PUT .../grid` redraws the map in place. `POST /api/maps/generate` lays down a
flat canvas of one terrain to author on top of — there is no procedural
generation.

Read next:

- **[docs/architecture.md](docs/architecture.md)** — identity, ownership, the
  event contracts, and why the services are split where they are
- **[docs/modules.md](docs/modules.md)** — what each service does, in detail
- **[docs/api.md](docs/api.md)** — every endpoint, with payloads
- **[CHANGELOG.md](CHANGELOG.md)** — what has changed

## Configuration

Every service reads its settings from environment variables with development
defaults, so the stack runs unconfigured.

| Variable                     | Used by                    | Default                          |
|------------------------------|----------------------------|----------------------------------|
| `SERVER_PORT`                | every service              | its own port, 8080-8085          |
| `TTARENA_JWT_PRIVATE_KEY`    | auth                       | `classpath:keys/dev-private.pem` |
| `TTARENA_JWT_PUBLIC_KEY`     | every service but auth     | `classpath:keys/dev-public.pem`  |
| `TTARENA_JWT_TTL_MINUTES`    | auth                       | `30`                             |
| `TTARENA_CORS_ORIGINS`       | auth, user                 | `http://localhost:3000`          |
| `MONGODB_URI`                | user, character, game, map | `mongodb://localhost:27017/<db>` |
| `REDIS_HOST` / `REDIS_PORT`  | matchmaking, game          | `localhost` / `6379`             |
| `REDIS_ENABLED`              | matchmaking, game          | `true`                           |
| `USER_SERVICE_BASE_URL`      | auth                       | `http://localhost:8081`          |
| `USER_SERVICE_TIMEOUT`       | auth                       | `5s`                             |
| `USER_SERVICE_MAX_RETRIES`   | auth                       | `1`                              |
| `USER_SERVICE_RETRY_BACKOFF` | auth                       | `200ms`                          |
| `CHARACTER_SERVICE_BASE_URL` | user, game                 | `http://localhost:8083`          |
| `CHARACTER_SERVICE_TIMEOUT`  | game                       | `5s`                             |
| `MAP_SERVICE_BASE_URL`       | game                       | `http://localhost:8085`          |
| `MAP_SERVICE_TIMEOUT`        | game                       | `5s`                             |
| `GAME_ARENA_MAP_ID`          | game                       | blank (no arena)                 |
| `GAME_MOVEMENT_PER_TURN`     | game                       | `4`                              |
| `GAME_TURN_TIMEOUT`          | game                       | `120` (seconds)                  |
| `MATCHMAKING_QUEUE_TTL`      | matchmaking                | `120` (seconds)                  |
| `MAP_MAX_RADIUS`             | map                        | `32` rings                       |
| `MAP_MAX_PER_OWNER`          | map                        | `50` maps                        |

One setting has no environment variable and is edited in `application.yml`:
`character.roster.max-size`, at 10 characters per account.

Only `auth` and `user` are reachable from a browser directly; the other four are
called by other services or through the gateway, which answers preflight for
every route, so they configure no CORS of their own.

`REDIS_ENABLED=false` drops the pub/sub wiring — publisher, template and
subscriber — so a context still starts without Redis. That is how the tests run.

> **The committed keypair is for development only.** Replace it before exposing
> anything — see [`auth/src/main/resources/keys/README.md`](auth/src/main/resources/keys/README.md).

## Health checks and API docs

Spring Boot Actuator is on every service. `/actuator/health` is the only
endpoint exposed by default and the only one reachable without a token; `auth`
additionally exposes `/actuator/info` and enables the liveness and readiness
probes, both of which still need one. Compose uses `mongosh` and `redis-cli`
health checks to order startup; the services themselves are started without one.

Swagger UI is served from `/swagger-ui.html` by `user`, `matchmaking`, `game`
and `map`. `auth` and `character` do not carry the springdoc dependency.
[docs/api.md](docs/api.md) documents every endpoint by hand, and remains the
complete reference.

## Testing

```bash
./gradlew test                  # everything
./gradlew :character:test       # one module
```

34 test classes, in every module: `character` 10, `map` 8, `auth` 7, `user` 4,
`game` 3, `matchmaking` 2. Gradle reports a good many more individual cases than
that, because several of the classes in `character` and `user` are parameterised
— `:user:test` alone runs 37.

Tests run without MongoDB or Redis: repositories and service clients are mocked,
the Redis wiring is switched off with `redis.enabled=false`, and a `test` profile
in `user`, `character`, `game` and `map` disables Mongo auto-index creation so
teardown stops logging `MongoClientException: Shutdown in progress`.

Most are unit tests. `auth` and `user` each add a `RANDOM_PORT` slice over the
real security chain, bound to a running server because CORS is partly an HTTP
concern that a context-bound client does not reproduce faithfully. Both guard
what has actually broken before: which paths are public, that a rejection is a
rejection, and that a preflight comes back with exactly one
`Access-Control-Allow-Origin`. `matchmaking` and `game` add a `contextLoads()`
smoke test.

## Tech stack

- **Back:** Spring Boot 4.1.1, Spring WebFlux, Spring Security (OAuth2 resource
  server), Spring Data MongoDB Reactive, Redis Pub/Sub, jjwt 0.13, springdoc 3.1
- **Build:** Gradle 9.7.1, Java 25 toolchain, Lombok
- **Infra:** Docker Compose, APISIX 3.11 standalone, MongoDB 8, Redis 7.4,
  `eclipse-temurin:25-jre-alpine` images running as a non-root user
- **Front:** Phaser.js (2D) or Three.js (3D) — decision pending
- **Planned:** WebSockets

## Known gaps

- A timed-out game stays `IN_PROGRESS` until someone claims the win, so an
  abandoned match lingers.
- Nothing stops a player queueing while already in a game: `join` checks that
  the character is on your roster and nothing else.
- Matchmaking state is in memory — a `ConcurrentLinkedQueue` plus a 1000-entry
  LRU of recent matches. A restart empties the queue, and a second instance
  would keep its own.
- The event payloads are duplicated by hand across modules with no shared
  contract (`RedisEvent` in `user` and `matchmaking`, `MatchFoundEvent` in
  `matchmaking` and `game`), so they can drift silently.
- `/actuator/health` hangs instead of reporting DOWN when MongoDB or Redis is
  unreachable — the Mongo driver waits out a 30-second server selection, and
  Lettuce queues its ping while it tries to reconnect rather than refusing it.
  Nothing sets `spring.data.mongodb.uri` timeouts or `spring.data.redis.timeout`
  outside the tests, so the endpoint outlives any probe that would call it.
- Password reset, email verification and token refresh do not exist.
- No end-to-end test: every module is covered without a database, and the
  cross-service calls have never run for real.
- `character`, `game`, `map` and `matchmaking` have no security test of their
  own; only `auth` and `user` guard their chain against regressions.
- The development keypair is committed, and the HS256 secret it replaced is
  still in the git history — treat that one as compromised.
- No license file.

## License

None yet. Until one is added, no permission to use, copy or distribute this
code is granted.
