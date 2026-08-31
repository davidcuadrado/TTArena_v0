package org.ttarena.matchmaking.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.ttarena.matchmaking.document.MatchFoundEvent;

/**
 * Redis pub/sub wiring. Everything here is switched off as a unit via the
 * {@code redis.enabled} property, so the application context still starts
 * (tests included) when Redis is not part of the picture.
 */
@Configuration
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisPubSubConfig {

    @Bean
    public ReactiveRedisTemplate<String, MatchFoundEvent> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        JacksonJsonRedisSerializer<MatchFoundEvent> valueSerializer =
                new JacksonJsonRedisSerializer<>(MatchFoundEvent.class);

        RedisSerializationContext<String, MatchFoundEvent> context =
                RedisSerializationContext.<String, MatchFoundEvent>newSerializationContext(new StringRedisSerializer())
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    /**
     * Listener container for subscribers. Declared here (rather than inside a
     * {@code @Service}) so Spring owns its lifecycle and closes the underlying
     * connection on shutdown.
     */
    @Bean(destroyMethod = "destroy")
    public ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisMessageListenerContainer(factory);
    }
}
