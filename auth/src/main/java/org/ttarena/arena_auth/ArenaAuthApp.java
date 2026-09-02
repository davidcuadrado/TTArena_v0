package org.ttarena.arena_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.ttarena.arena_auth.config.JwtProperties;
import org.ttarena.arena_auth.config.UserServiceProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, UserServiceProperties.class})
public class ArenaAuthApp {

	public static void main(String[] args) {
		SpringApplication.run(ArenaAuthApp.class, args);
	}

}
