package org.ttarena.matchmaking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.ttarena.matchmaking.document.MatchFoundEvent;

@Configuration
public class RedisPubSubConfig {

    @Bean
    public ReactiveRedisTemplate<String, MatchFoundEvent> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        // Configure ObjectMapper for polymorphic typing
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        // Use new constructor instead of setObjectMapper()
        Jackson2JsonRedisSerializer<MatchFoundEvent> valueSerializer =
                new Jackson2JsonRedisSerializer<>(MatchFoundEvent.class);

        RedisSerializationContext<String, MatchFoundEvent> context =
                RedisSerializationContext.<String, MatchFoundEvent>newSerializationContext(new StringRedisSerializer())
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
