# TTArena

A turn-based arena game backend: players register, build characters from
thirteen classes, queue for a match, and fight one turn at a time.

Six independent Spring Boot services, reactive throughout (WebFlux + reactive
MongoDB), talking to each other over HTTP and Redis pub/sub.

## Requirements

- **JDK 25** — or none, and let Gradle download one (the foojay resolver is
  configured in `settings.gradle.kts`)
- **MongoDB** on `localhost:27017`
- **Redis** on `localhost:6379`

```bash
docker run -d -p 27017:27017 --name ttarena-mongo mongo:8
docker run -d -p 6379:6379   --name ttarena-redis redis:8
```

## Quick start

```bash
./gradlew clean build                       # compile + test everything
./gradlew :user:bootRun                     # one service at a time
./gradlew :auth:bootRun
./gradlew :character:bootRun --args='--spring.profiles.active=dev'
./gradlew :matchmaking:bootRun
./gradlew :game:bootRun
```

The `dev` profile on `character` wipes and reseeds its collections with sample
characters and abilities.

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

| Module        | Purpose                                  | Port | Storage       | State             |
|---------------|------------------------------------------|------|---------------|-------------------|
| `auth`        | Login, mints JWTs                        | 8080 | none          | Working           |
| `user`        | Accounts, registration, queue entry      | 8081 | MongoDB       | Working           |
| `matchmaking` | Pairs waiting players                    | 8082 | Redis         | Working           |
| `character`   | Characters, abilities, combat resolution | 8083 | MongoDB       | Most complete     |
| `game`        | Match sessions and turn order            | 8084 | MongoDB+Redis | Working           |
| `map`         | Hex arenas, terrain and pathfinding      | 8085 | MongoDB       | Working           |

Every module is its own Spring Boot application with its own
`build.gradle.kts` and boot jar. The root project is a container only.

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

Read next:

- **[docs/architecture.md](docs/architecture.md)** — identity, ownership, the
  event contracts, and why the services are split where they are
- **[docs/modules.md](docs/modules.md)** — what each service does, in detail
- **[docs/api.md](docs/api.md)** — every endpoint, with payloads
- **[CHANGELOG.md](CHANGELOG.md)** — what has changed

## Configuration

Every service reads its settings from environment variables with development
defaults, so the stack runs unconfigured.

| Variable                     | Used by                    | Default                              |
|------------------------------|----------------------------|--------------------------------------|
| `TTARENA_JWT_PRIVATE_KEY`    | auth                       | `classpath:keys/dev-private.pem`     |
| `TTARENA_JWT_PUBLIC_KEY`     | every service but auth     | `classpath:keys/dev-public.pem`      |
| `MONGODB_URI`                | user, character, game, map | `mongodb://localhost:27017/<db>`     |
| `REDIS_HOST` / `REDIS_PORT`  | matchmaking, game          | `localhost` / `6379`                 |
| `USER_SERVICE_BASE_URL`      | auth                       | `http://localhost:8081`              |
| `CHARACTER_SERVICE_BASE_URL` | user, game                 | `http://localhost:8083`              |
| `MATCHMAKING_QUEUE_TTL`      | matchmaking                | `120` (seconds)                      |
| `GAME_TURN_TIMEOUT`          | game                       | `120` (seconds)                      |
| `MAP_MAX_RADIUS`             | map                        | `32` rings                           |
| `MAP_MAX_PER_OWNER`          | map                        | `50` maps                            |

> **The committed keypair is for development only.** Replace it before exposing
> anything — see [`auth/src/main/resources/keys/README.md`](auth/src/main/resources/keys/README.md).

## Testing

```bash
./gradlew test                  # everything
./gradlew :character:test       # one module
```

Tests run without MongoDB or Redis: repositories and service clients are mocked,
and the Redis wiring is switched off with `redis.enabled=false`.

## Tech stack

- **Back:** Spring Boot 4.1.1, Spring WebFlux, Spring Security (OAuth2 resource
  server), Spring Data MongoDB Reactive, Redis Pub/Sub
- **Build:** Gradle 9.7.1, Java 25 toolchain
- **Front:** Phaser.js (2D) or Three.js (3D) — decision pending
- **Planned:** WebSockets, Docker Compose, APISIX as gateway

## Known gaps

- A timed-out game stays `IN_PROGRESS` until someone claims the win, so an
  abandoned match lingers.
- Nothing stops a player queueing while already in a game.
- Matchmaking state is in memory: a restart empties the queue, and a second
  instance would keep its own.
- The event payloads are duplicated by hand in three modules with no shared
  contract, so they can drift silently.
- Only `matchmaking` has a Dockerfile; there is no Compose file.
- Password reset, email verification and token refresh do not exist.
- No slice or integration tests: every service is covered by unit tests plus a
  `contextLoads()`, and the cross-service calls have never run for real.
- No license file.
