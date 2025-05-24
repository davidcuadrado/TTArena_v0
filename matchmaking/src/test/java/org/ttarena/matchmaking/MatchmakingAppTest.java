package org.ttarena.matchmaking;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.ttarena.matchmaking.config.RedisConfigTest;
import org.ttarena.matchmaking.service.MatchFoundPublisher;

@SpringBootTest
@Import(RedisConfigTest.class)
@ActiveProfiles("test")
public class MatchmakingAppTest {

    // Aseguramos que estos beans estén disponibles en el contexto de prueba
    @Mock
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @Mock
    private MatchFoundPublisher matchFoundPublisher;

    @Test
    void contextLoads() {
        // El contexto de Spring debería cargarse correctamente con los mocks
    }
}
