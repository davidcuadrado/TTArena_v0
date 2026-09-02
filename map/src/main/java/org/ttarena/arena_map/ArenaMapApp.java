package org.ttarena.arena_map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.ttarena.arena_map.config.MapProperties;

@SpringBootApplication
@EnableConfigurationProperties(MapProperties.class)
public class ArenaMapApp {

    public static void main(String[] args) {
        SpringApplication.run(ArenaMapApp.class, args);
    }
}
