# API

Every endpoint except registration and login needs `Authorization: Bearer <jwt>`.

## auth — `:8080`

| Method | Path          | Auth   | Body                     | Returns              |
|--------|---------------|--------|--------------------------|----------------------|
| POST   | `/auth/login` | public | `{username, password}`   | `{token}`, 401 if bad |

## user — `:8081`

| Method | Path                  | Auth   | Notes                                                    |
|--------|-----------------------|--------|----------------------------------------------------------|
| POST   | `/user/register`      | public | `{username, email, password}` → 201                       |
| POST   | `/users/authenticate` | public | service-to-service, used by `auth`                        |
| GET    | `/user/home`          | USER   |                                                           |
| POST   | `/user/queue/join`    | USER   | `{characterId}` — must be on your roster                  |
| POST   | `/user/queue/leave`   | USER   | publishes `USER_DISCONNECTED`                             |

Username: 3–32 characters, letters and digits only.
Password: at least 8 characters. Email must be valid.

## character — `:8083`

| Method | Path                                   | Notes                                     |
|--------|----------------------------------------|-------------------------------------------|
| GET    | `/api/characters`                      | all characters                            |
| GET    | `/api/characters/me`                   | your roster only                          |
| GET    | `/api/characters/{id}`                 | 404 if unknown                            |
| GET    | `/api/characters/name/{name}`          | 404 if unknown                            |
| GET    | `/api/characters/class/{characterClass}` | e.g. `/class/WARRIOR`                   |
| POST   | `/api/characters`                      | creates any class → 201                   |
| PUT    | `/api/characters/{id}`                 | rename only, yours only                   |
| DELETE | `/api/characters/{id}`                 | yours only → 204                          |
| GET    | `/api/abilities`                       | all abilities                             |
| GET    | `/api/abilities/{id}`                  |                                           |
| GET    | `/api/abilities/class/{characterClass}` |                                          |
| GET    | `/api/abilities/class/{characterClass}/specialization/{specialization}` |          |
| POST   | `/api/abilities/cast`                  | `{casterId, abilityId, targetIds}`        |

`PUT` changes the name and nothing else. Health and resources are combat state;
they move through a cast.

## matchmaking — `:8082`

| Method | Path                    | Notes                                            |
|--------|-------------------------|--------------------------------------------------|
| GET    | `/api/matchmaking/me`   | queued? queue size? last match and opponent      |

## game — `:8084`

| Method | Path                            | Notes                                    |
|--------|---------------------------------|------------------------------------------|
| GET    | `/api/games/me`                 | your game in progress, 404 if none        |
| GET    | `/api/games`                    | every game you have played                |
| GET    | `/api/games/{id}`               | 403 unless you are one of the two players |
| POST   | `/api/games/{id}/cast`          | `{abilityId}` — plays one turn            |
| POST   | `/api/games/{id}/surrender`     | opponent wins immediately                 |
| POST   | `/api/games/{id}/claim-timeout` | win when your opponent's turn ran out      |
| POST   | `/api/games/{id}/rematch`       | same players, loser moves first           |

## map — `:8085`

Reads need only a valid token; every write is owner-only.

| Method | Path                                   | Notes                                              |
|--------|----------------------------------------|----------------------------------------------------|
| GET    | `/api/maps`                            | all maps, or `?name=` to filter                     |
| GET    | `/api/maps/me`                         | the maps you own                                    |
| GET    | `/api/maps/{id}`                       | one map with its tiles                              |
| POST   | `/api/maps`                            | `{name, description}` — an empty map                |
| POST   | `/api/maps/generate`                   | `{name, description, radius, terrain}`               |
| PUT    | `/api/maps/{id}`                       | `{name, description}`, both optional                 |
| DELETE | `/api/maps/{id}`                       | 403 unless you own it                                |
| GET    | `/api/maps/{id}/tiles/{q}/{r}/{s}`     | one tile, 400 if `q+r+s != 0`                        |
| PUT    | `/api/maps/{id}/tiles/{q}/{r}/{s}`     | `{terrain, elevation}` — create or replace           |
| DELETE | `/api/maps/{id}/tiles/{q}/{r}/{s}`     | 404 if there is no tile there                        |
| GET    | `/api/maps/{id}/path?from=&to=`        | A* route, coordinates as `q:r:s`                     |

Omitting `terrain` from `/generate` produces mixed random terrain. The path
endpoint answers `{path, movementCost, reachable}` and reports an unreachable
goal as `reachable: false` rather than as an error.

## A full run

```bash
# 1. two accounts
curl -X POST localhost:8081/user/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@ttarena.org","password":"password123"}'
curl -X POST localhost:8081/user/register -H 'Content-Type: application/json' \
  -d '{"username":"bob","email":"bob@ttarena.org","password":"password123"}'

# 2. tokens
ALICE=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"password123"}' | jq -r .token)
BOB=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"bob","password":"password123"}' | jq -r .token)

# 3. a character each
curl -X POST localhost:8083/api/characters -H "Authorization: Bearer $ALICE" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Conan","characterClass":"WARRIOR","health":200,"resourceAmount":100,"specialization":"ARMS"}'
curl -X POST localhost:8083/api/characters -H "Authorization: Bearer $BOB" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Anduin","characterClass":"PRIEST","health":200,"resourceAmount":100,"specialization":"HOLY"}'

# 4. queue both (use the ids from step 3)
curl -X POST localhost:8081/user/queue/join -H "Authorization: Bearer $ALICE" \
  -H 'Content-Type: application/json' -d '{"characterId":"<alice-character-id>"}'
curl -X POST localhost:8081/user/queue/join -H "Authorization: Bearer $BOB" \
  -H 'Content-Type: application/json' -d '{"characterId":"<bob-character-id>"}'

# 5. the session both players now share
curl localhost:8084/api/games/me -H "Authorization: Bearer $ALICE"

# 6. Alice plays a turn (ability ids come from /api/abilities/class/WARRIOR)
curl -X POST localhost:8084/api/games/<game-id>/cast -H "Authorization: Bearer $ALICE" \
  -H 'Content-Type: application/json' -d '{"abilityId":"<ability-id>"}'
```

Swagger UI is on every service that serves HTTP, at `/swagger-ui.html`.
