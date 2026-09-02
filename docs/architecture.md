# Architecture

## Service boundaries

| Service       | Owns                                              |
|---------------|---------------------------------------------------|
| `auth`        | Token minting. The only holder of the private key |
| `user`        | Accounts and credentials; queue entry             |
| `matchmaking` | Who is waiting for a match                        |
| `character`   | Characters, abilities, and what a cast does       |
| `game`        | Sessions: who plays, whose turn, how it ended     |

Each service owns its own data and its own database. No service reads another's
collections; they talk over HTTP or Redis.

## Identity

```
POST /user/register              create the account                (user, public)
POST /auth/login                 -> auth calls user's
                                    POST /users/authenticate        (service to service)
                                 <- JWT { sub: username,
                                          userId: <uuid>,
                                          roles: [...] }
Authorization: Bearer <jwt>      -> every other service verifies it itself
```

- `userId` is a UUID generated at registration and stored as the account's
  `_id`. It is the ownership key everywhere, so renaming an account never
  orphans its characters.
- Tokens are **RS256**. `auth` signs with the private key; `user`, `character`,
  `matchmaking` and `game` hold only the public key. A service that is
  compromised cannot forge an identity.
- Keys are PEM resources: `ttarena.jwt.private-key` (auth only) and
  `ttarena.jwt.public-key` (everywhere else).
- `ArenaUserPrincipal` (user) and `AuthenticatedUserPrincipal` (auth) exist to
  carry the UUID into the token. Spring's own `User` drops it, which would
  leave the `userId` claim repeating the username.

## Ownership

An account owns characters, and may only play as its own.

| Action                          | Rule                                              |
|---------------------------------|---------------------------------------------------|
| Create a character              | Stamped with your `userId`; roster limit applies  |
| Update / delete a character     | Yours only, enforced by the query                 |
| Queue with a character          | Checked against your roster before the event      |
| Cast an ability **as** a caster | Yours only, else 403                              |
| Target another character        | Anyone — you must be able to attack opponents     |
| Read a character                | Any authenticated user — you must see opponents   |

Two decisions worth keeping:

**Ownership is a query constraint, not a check.** `findByIdAndOwnerId` rather
than `findById` followed by a comparison. A forgotten check becomes a missing
row instead of a security hole, and there is no window between loading and
verifying.

**The owner comes from the token, never the body.** No request shape can name an
owner, so there is nothing to forge.

## Enforcement is layered

The game module decides *whose turn it is*. The character service decides *who
owns what*. When `game` resolves a cast it forwards the player's own token to
`character`, which re-applies its ownership rule and its cast rules — so a bug
in the turn check cannot let a player cast with someone else's character.

Likewise, `user` verifies a character belongs to you before publishing a queue
event, because matchmaking has no token and cannot check, and a match made with
a character you do not own produces a session nobody can play.

## Event contracts

Both channels carry JSON strings with string serializers on key and value.

`user.status.<userId>` — published by `user`, consumed by `matchmaking`:

```json
{ "type": "USER_CONNECTED", "userId": "…", "characterId": "…", "timestamp": "…" }
```

`type` is `USER_CONNECTED` (join) or `USER_DISCONNECTED` (leave).

`match.found` — published by `matchmaking`, consumed by `game`:

```json
{
  "type": "MATCH_FOUND",
  "participants": [
    { "userId": "…", "characterId": "…" },
    { "userId": "…", "characterId": "…" }
  ],
  "timestamp": "…"
}
```

> These records are duplicated by hand in each module. There is no shared
> contract module yet, so a change on one side has to be mirrored on the other.

## Patterns in use

| Pattern                | Where                                      | Why                                                        |
|------------------------|--------------------------------------------|------------------------------------------------------------|
| Factory + registry     | `character/factory`                        | 13 classes built without the service knowing any type      |
| Strategy               | `character/combat` `AbilityEffect`         | Damage/heal per ability type; BUFF arrives as a new bean    |
| Chain of responsibility| `character/combat` `CastRule`              | Four ordered cast preconditions, each its own class         |
| Policy object          | `character` `RosterPolicy`                 | "At most N characters per account" in one place             |
| Adapter                | `ArenaUserPrincipal`                       | Carries the account UUID into Spring Security               |
| Enum as data           | the 13 `*Specialization` enums             | Stats live beside the role they belong with                 |

Two registries, deliberately different: `CharacterFactoryRegistry` fails at
startup if a `CharacterClass` has no factory, because full coverage is required;
`AbilityEffectRegistry` tolerates gaps, because BUFF and DEBUFF are legitimately
unimplemented.

## Validation

Character names are letters only (`^\p{L}+$`, max 32); usernames are
alphanumeric (`^[\p{L}\p{N}]+$`, 3–32). Unicode classes, so `Conán` and `Ñuria`
pass while digits, spaces and punctuation do not.

Each rule is enforced twice on purpose: as bean validation on the request
record, which produces a readable 400, and inside the domain — constructor,
setter and builder — so a seeder or migration cannot write what the API would
reject. Reads are *not* validated: `name` and `username` use
`@AccessType(FIELD)`, so Spring Data writes the field directly when loading and
a legacy document stays readable.
