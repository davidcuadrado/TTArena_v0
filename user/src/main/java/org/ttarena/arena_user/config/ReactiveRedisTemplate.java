package org.ttarena.arena_user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.ttarena.arena_user.document.ArenaUserDocument;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class ReactiveRedisTemplate {

    @Bean
    public org.springframework.data.redis.core.ReactiveRedisTemplate<String, ArenaUserDocument> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        Jackson2JsonRedisSerializer<ArenaUserDocument> serializer = new Jackson2JsonRedisSerializer<>(ArenaUserDocument.class);
        RedisSerializationContext<String, ArenaUserDocument> context = RedisSerializationContext
                .<String, ArenaUserDocument>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new org.springframework.data.redis.core.ReactiveRedisTemplate<>(factory, context);
    }
}