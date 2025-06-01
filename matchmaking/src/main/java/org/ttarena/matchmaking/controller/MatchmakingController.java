package org.ttarena.matchmaking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.ttarena.matchmaking.service.RedisMatchmakingService;

@Slf4j
@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

    private final RedisMatchmakingService matchmakingService;

    @Autowired
    public MatchmakingController(RedisMatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @PostMapping("/queue/{userId}")
    public Mono<ResponseEntity<String>> enqueueUser(@PathVariable String userId) {
        log.info("Request to enqueue user: {}", userId);
        return matchmakingService.enqueueUser(userId)
                .map(result -> ResponseEntity.ok("User " + userId + " added to matchmaking queue"))
                .defaultIfEmpty(ResponseEntity.badRequest().body("Failed to add user to queue"));
    }

    @DeleteMapping("/queue/{userId}")
    public Mono<ResponseEntity<String>> dequeueUser(@PathVariable String userId) {
        log.info("Request to dequeue user: {}", userId);
        return matchmakingService.dequeueUser(userId)
                .map(result -> ResponseEntity.ok("User " + userId + " removed from matchmaking queue"))
                .defaultIfEmpty(ResponseEntity.badRequest().body("Failed to remove user from queue"));
    }

    @GetMapping("/queue/size")
    public Mono<ResponseEntity<Long>> getQueueSize() {
        return matchmakingService.getQueueSize()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/queue/users")
    public Mono<ResponseEntity<Flux<String>>> getWaitingUsers() {
        return Mono.just(ResponseEntity.ok(matchmakingService.getWaitingUsers()));
    }
}
