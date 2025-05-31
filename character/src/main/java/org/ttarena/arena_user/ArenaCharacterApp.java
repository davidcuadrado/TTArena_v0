package org.ttarena.arena_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class ArenaCharacterApp {

	public static void main(String[] args) {
		SpringApplication.run(ArenaCharacterApp.class, args);
	}

}
