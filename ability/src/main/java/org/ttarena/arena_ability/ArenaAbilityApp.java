package org.ttarena.arena_ability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
public class ArenaAbilityApp {

    public static void main(String[] args) {
        SpringApplication.run(ArenaAbilityApp.class, args);
    }
}

