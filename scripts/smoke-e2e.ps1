<#
    Walks one real match across every service, so the cross-service calls run
    for real instead of being asserted about in unit tests.

        docker compose up -d --build
        .\scripts\smoke-e2e.ps1

    Stops at the first failure and prints the ProblemDetail the service
    returned. Re-runnable: accounts are suffixed with a timestamp.
#>
[CmdletBinding()]
param(
    [string]$AuthUrl        = "http://localhost:8080",
    [string]$UserUrl        = "http://localhost:8081",
    [string]$MatchmakingUrl = "http://localhost:8082",
    [string]$CharacterUrl   = "http://localhost:8083",
    [string]$GameUrl        = "http://localhost:8084",
    [string]$MapUrl         = "http://localhost:8085",
    [switch]$SkipGameRecreate
)

$ErrorActionPreference = "Stop"
$script:step = 0

function Step([string]$text) {
    $script:step++
    Write-Host ""
    Write-Host ("[{0}] {1}" -f $script:step, $text) -ForegroundColor Cyan
}

function Ok([string]$text)   { Write-Host "    ok   $text" -ForegroundColor Green }
function Note([string]$text) { Write-Host "    ..   $text" -ForegroundColor DarkGray }

function Fail([string]$text) {
    Write-Host "    FAIL $text" -ForegroundColor Red
    exit 1
}

function Api {
    param(
        [string]$Method,
        [string]$Uri,
        $Body,
        [string]$Token,
        [string]$ContentType = "application/json"
    )

    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }

    $request = @{ Method = $Method; Uri = $Uri; Headers = $headers }
    if ($null -ne $Body) {
        $request["ContentType"] = $ContentType
        $request["Body"] = if ($Body -is [string]) { $Body } else { $Body | ConvertTo-Json -Depth 10 -Compress }
    }

    try {
        return Invoke-RestMethod @request
    }
    catch {
        $status = $null
        if ($_.Exception.Response) { $status = [int]$_.Exception.Response.StatusCode }
        $detail = $_.ErrorDetails.Message
        if (-not $detail) { $detail = $_.Exception.Message }

        Write-Host "    FAIL $Method $Uri -> $status" -ForegroundColor Red
        Write-Host "         $detail" -ForegroundColor Red
        exit 1
    }
}

function WaitForHealth([string]$name, [string]$url) {
    for ($i = 1; $i -le 60; $i++) {
        try {
            $health = Invoke-RestMethod -Uri "$url/actuator/health" -TimeoutSec 3
            if ($health.status -eq "UP") { Ok "$name is up"; return }
        } catch { }
        Start-Sleep -Seconds 2
    }
    Fail "$name never reported healthy at $url/actuator/health"
}

# ---------------------------------------------------------------- 1. health
Step "Waiting for every service to report healthy"
WaitForHealth "auth"        $AuthUrl
WaitForHealth "user"        $UserUrl
WaitForHealth "matchmaking" $MatchmakingUrl
WaitForHealth "character"   $CharacterUrl
WaitForHealth "game"        $GameUrl
WaitForHealth "map"         $MapUrl

# ------------------------------------------------------------- 2. accounts
$stamp  = Get-Date -Format "HHmmss"
$alice  = "alice$stamp"
$bob    = "bob$stamp"
$secret = "password123"

Step "Registering two accounts through the user service"
Api POST "$UserUrl/user/register" @{ username = $alice; email = "$alice@ttarena.org"; password = $secret } | Out-Null
Ok "registered $alice"
Api POST "$UserUrl/user/register" @{ username = $bob; email = "$bob@ttarena.org"; password = $secret } | Out-Null
Ok "registered $bob"

Step "Logging in through auth, which calls user to verify the credentials"
$aliceToken = (Api POST "$AuthUrl/auth/login" @{ username = $alice; password = $secret }).token
$bobToken   = (Api POST "$AuthUrl/auth/login" @{ username = $bob;   password = $secret }).token
if (-not $aliceToken -or -not $bobToken) { Fail "auth returned no token" }
Ok "both tokens issued (auth -> user hop works)"

# ------------------------------------------------------------ 3. characters
Step "Creating one character each, owned by the caller's token"
$aliceChar = Api POST "$CharacterUrl/api/characters" `
    @{ name = "Thunderpaw"; characterClass = "SHAMAN"; health = 200; resourceAmount = 150; specialization = "ELEMENTAL" } `
    -Token $aliceToken
$bobChar = Api POST "$CharacterUrl/api/characters" `
    @{ name = "Stonefist"; characterClass = "SHAMAN"; health = 200; resourceAmount = 150; specialization = "ELEMENTAL" } `
    -Token $bobToken
Ok "alice -> $($aliceChar.id), bob -> $($bobChar.id)"

Step "Fetching a ranged ability from the seeded catalogue"
$abilities = Api GET "$CharacterUrl/api/abilities/class/SHAMAN/specialization/ELEMENTAL" -Token $aliceToken
if (-not $abilities) { Fail "no abilities seeded - is character running with SPRING_PROFILES_ACTIVE=dev?" }
$ability = $abilities | Sort-Object -Property range -Descending | Select-Object -First 1
Ok "using '$($ability.name)' (range $($ability.range), cost $($ability.resourceCost))"

# ----------------------------------------------------------------- 4. arena
Step "Importing a hand-authored arena"
$arena = @{
    name        = "Smoke Arena"
    description = "Flat ground with a few features, for the end to end run"
    radius      = 3
    legend      = @{ "." = "PLAIN"; "f" = "FOREST"; "^" = "MOUNTAIN"; "~" = "WATER" }
    grid        = @(
        ". . . .",
        ". . f . .",
        ". f . . . .",
        ". . . . . . .",
        ". . . . ^ .",
        ". . ~ . .",
        ". . . ."
    )
}
$map = Api POST "$MapUrl/api/maps/import" $arena -Token $aliceToken
Ok "arena $($map.id) imported with $($map.tileCount) tiles"

Step "Asking the map service where players would start"
$starts = Api GET "$MapUrl/api/maps/$($map.id)/starting-positions?count=2" -Token $aliceToken
Ok "starts $($starts[0].q):$($starts[0].r):$($starts[0].s) and $($starts[1].q):$($starts[1].r):$($starts[1].s)"

# ------------------------------------------------- 5. point game at the arena
$arenaInUse = $env:GAME_ARENA_MAP_ID
if ($arenaInUse -ne $map.id -and -not $SkipGameRecreate) {
    Step "Pointing the game service at this arena and recreating it"
    "GAME_ARENA_MAP_ID=$($map.id)" | Set-Content -Path ".env" -Encoding ascii
    $env:GAME_ARENA_MAP_ID = $map.id
    docker compose up -d --force-recreate --no-deps game | Out-Null
    if ($LASTEXITCODE -ne 0) { Fail "docker compose could not recreate the game service" }
    Note "waiting for game to come back"
    Start-Sleep -Seconds 3
    WaitForHealth "game" $GameUrl
}

# ----------------------------------------------------------------- 6. a match
Step "Both players join the queue; matchmaking should pair them"
Api POST "$UserUrl/user/queue/join" @{ characterId = $aliceChar.id } -Token $aliceToken | Out-Null
Ok "$alice queued"
Api POST "$UserUrl/user/queue/join" @{ characterId = $bobChar.id } -Token $bobToken | Out-Null
Ok "$bob queued"

Step "Waiting for game to consume match.found off Redis"
$game = $null
for ($i = 1; $i -le 20; $i++) {
    Start-Sleep -Seconds 1
    try { $game = Invoke-RestMethod -Uri "$GameUrl/api/games/me" -Headers @{ Authorization = "Bearer $aliceToken" } } catch { }
    if ($game) { break }
}
if (-not $game) { Fail "no game session appeared - check 'docker compose logs matchmaking game'" }
Ok "game $($game.id), turn $($game.turnNumber), arena $($game.arenaMapId)"
Ok "alice at $($game.yourPosition.q):$($game.yourPosition.r):$($game.yourPosition.s), opponent at $($game.opponentPosition.q):$($game.opponentPosition.r):$($game.opponentPosition.s)"

$onTurn      = if ($game.yourTurn) { $aliceToken } else { $bobToken }
$onTurnName  = if ($game.yourTurn) { $alice } else { $bob }
$view        = Api GET "$GameUrl/api/games/$($game.id)" -Token $onTurn
Note "$onTurnName moves first with $($view.yourMovementRemaining) movement"

# --------------------------------------------------- 7. range, move, then cast
$distance = [Math]::Max([Math]::Abs($view.yourPosition.q - $view.opponentPosition.q),
            [Math]::Max([Math]::Abs($view.yourPosition.r - $view.opponentPosition.r),
                        [Math]::Abs($view.yourPosition.s - $view.opponentPosition.s)))
Step "Distance between players is $distance; '$($ability.name)' reaches $($ability.range)"

if ($distance -gt $ability.range) {
    Note "expecting the cast to be refused by RangeRule in the character service"
    try {
        Invoke-RestMethod -Method POST -Uri "$GameUrl/api/games/$($game.id)/cast" `
            -Headers @{ Authorization = "Bearer $onTurn" } -ContentType "application/json" `
            -Body (@{ abilityId = $ability.id } | ConvertTo-Json) | Out-Null
        Fail "an out-of-range cast succeeded - RangeRule is not being applied"
    }
    catch {
        Ok "refused: $($_.ErrorDetails.Message)"
    }

    Step "Stepping one hex toward the opponent, then casting again"
    $toward = @{
        q = $view.yourPosition.q + [Math]::Sign($view.opponentPosition.q - $view.yourPosition.q)
        r = $view.yourPosition.r + [Math]::Sign($view.opponentPosition.r - $view.yourPosition.r)
    }
    $toward.s = -$toward.q - $toward.r
    $moved = Api POST "$GameUrl/api/games/$($game.id)/move" $toward -Token $onTurn
    Ok "moved to $($moved.yourPosition.q):$($moved.yourPosition.r):$($moved.yourPosition.s), $($moved.yourMovementRemaining) movement left"
}

Step "Casting for real"
$after = Api POST "$GameUrl/api/games/$($game.id)/cast" @{ abilityId = $ability.id } -Token $onTurn
if (-not $after) { Fail "the cast returned no session" }
$turn = $after.turns[-1]
Ok "$($turn.abilityName) hit for $($turn.amount); target left on $($turn.targetRemainingHealth) hp"
Ok "turn passed to $($after.currentTurnUserId), now turn $($after.turnNumber)"

Write-Host ""
Write-Host "End to end run complete: auth -> user -> character -> map -> matchmaking -> game all answered." -ForegroundColor Green
