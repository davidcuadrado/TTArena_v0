package org.ttarena.arena_user.config;

// No JavaType import needed if using GenericJackson2JsonRedisSerializer this way
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.ttarena.arena_user.document.ArenaUserDocument;

import java.util.Map;

@Configuration
public class AppRedisConfiguration {

    @Bean(name = "userDocumentRedisTemplate")
    public ReactiveRedisTemplate<String, ArenaUserDocument> userDocumentRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        ObjectMapper userObjectMapper = new ObjectMapper();

        Jackson2JsonRedisSerializer<ArenaUserDocument> serializer = new Jackson2JsonRedisSerializer<>(ArenaUserDocument.class);

        RedisSerializationContext<String, ArenaUserDocument> context = RedisSerializationContext
                .<String, ArenaUserDocument>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean(name = "mapRedisTemplate")
    public ReactiveRedisTemplate<String, Map<String, Object>> mapRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        objectMapper.findAndRegisterModules();

        RedisSerializer<Object> genericSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisSerializationContext<String, Map<String, Object>> context = RedisSerializationContext
                .<String, Map<String, Object>>newSerializationContext(new StringRedisSerializer())
                .value((RedisSerializer<Map<String, Object>>) (RedisSerializer<?>) genericSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}