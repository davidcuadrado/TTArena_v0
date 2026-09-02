package org.ttarena.matchmaking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context smoke test. Redis is switched off (redis.enabled=false) so the context
 * loads without a running Redis server and without a ReactiveRedisConnectionFactory.
 *
 * <p>Since Spring Boot 4 these auto-configurations live in
 * {@code org.springframework.boot.data.redis.autoconfigure} and carry a
 * {@code Data} prefix.
 */
@SpringBootTest(properties = "redis.enabled=false")
@ImportAutoConfiguration(exclude = {
        DataRedisAutoConfiguration.class,
        DataRedisReactiveAutoConfiguration.class,
        DataRedisRepositoriesAutoConfiguration.class
})
public class MatchmakingAppTest {

    @Test
    void contextLoads() {
        // Context must start cleanly.
    }
}
