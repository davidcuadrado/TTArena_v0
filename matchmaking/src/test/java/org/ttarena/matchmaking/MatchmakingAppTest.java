package org.ttarena.matchmaking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context smoke test. Redis is switched off (redis.enabled=false) so the context
 * loads without a running Redis server and without a ReactiveRedisConnectionFactory.
 */
@SpringBootTest(properties = "redis.enabled=false")
@ImportAutoConfiguration(exclude = {
        RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
public class MatchmakingAppTest {

    @Test
    void contextLoads() {
        // Context must start cleanly.
    }
}
