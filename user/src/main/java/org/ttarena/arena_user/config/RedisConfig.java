package org.ttarena.arena_user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.ttarena.arena_user.document.ArenaUserDocument;

@Configuration
public class RedisConfig {

    /**
     * Template for storing {@link ArenaUserDocument} values.
     *
     * <p>Deliberately not named {@code reactiveRedisTemplate}: that name belongs to
     * Spring Boot's auto-configured {@code ReactiveRedisTemplate<Object, Object>},
     * and reusing it here would silently replace it.
     */
    @Bean
    public ReactiveRedisTemplate<String, ArenaUserDocument> arenaUserRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        Jackson2JsonRedisSerializer<ArenaUserDocument> serializer =
                new Jackson2JsonRedisSerializer<>(ArenaUserDocument.class);

        RedisSerializationContext<String, ArenaUserDocument> context = RedisSerializationContext
                .<String, ArenaUserDocument>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
