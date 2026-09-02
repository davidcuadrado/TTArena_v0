package org.ttarena.matchmaking.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import org.ttarena.matchmaking.document.MatchFoundEvent;
import org.ttarena.matchmaking.service.MatchFoundPublisher;

@TestConfiguration
@Profile("test")
@ImportAutoConfiguration(exclude = {RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class})
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
public class RedisConfigTest {

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return Mockito.mock(ReactiveRedisConnectionFactory.class);
    }

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate() {
        ReactiveRedisTemplate<String, String> template = Mockito.mock(ReactiveRedisTemplate.class);

        ReactiveSetOperations<String, String> setOps = Mockito.mock(ReactiveSetOperations.class);
        Mockito.when(template.opsForSet()).thenReturn(setOps);

        ReactiveValueOperations<String, String> valueOps = Mockito.mock(ReactiveValueOperations.class);
        Mockito.when(template.opsForValue()).thenReturn(valueOps);

        return template;
    }

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, MatchFoundEvent> reactiveRedisTemplate() {
        ReactiveRedisTemplate<String, MatchFoundEvent> template = Mockito.mock(ReactiveRedisTemplate.class);

        Mockito.when(template.convertAndSend(Mockito.anyString(), Mockito.any(MatchFoundEvent.class)))
                .thenReturn(Mono.just(1L));
        return template;
    }

    @Bean
    @Primary
    public MatchFoundPublisher matchFoundPublisher() {
        MatchFoundPublisher publisher = Mockito.mock(MatchFoundPublisher.class);
        Mockito.when(publisher.publishMatch(Mockito.any(MatchFoundEvent.class)))
                .thenReturn(Mono.empty());
        return publisher;
    }
}
