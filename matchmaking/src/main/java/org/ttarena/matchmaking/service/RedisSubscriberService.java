package org.ttarena.matchmaking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.stereotype.Service;
import org.ttarena.matchmaking.util.RedisEvent;
import java.util.Collections;


@Slf4j
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
@Profile("!test")
public class RedisSubscriberService {

    private final ReactiveRedisMessageListenerContainer container;
    private final MatchmakingServiceAdapter matchmakingServiceAdapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RedisSubscriberService(ReactiveRedisConnectionFactory connectionFactory,
                                  MatchmakingServiceAdapter matchmakingServiceAdapter) {
        this.container = new ReactiveRedisMessageListenerContainer(connectionFactory);
        this.matchmakingServiceAdapter = matchmakingServiceAdapter;
    }

    @PostConstruct
    public void subscribeToUserStatusEvents() {
        container.receive(
                Collections.singletonList(new ChannelTopic("user.status.*")),
                RedisSerializationContext.string().getKeySerializationPair(),
                RedisSerializationContext.string().getValueSerializationPair()
        ).doOnNext(message -> {
            String channel = message.getChannel();
            String rawMessage = message.getMessage();
            log.info("Received message on {}: {}", channel, rawMessage);

            try {
                RedisEvent event = objectMapper.readValue(rawMessage, RedisEvent.class);
                matchmakingServiceAdapter.processUserEvent(event).subscribe();
            } catch (Exception e) {
                log.error("Error processing Redis message: {}", e.getMessage(), e);
            }
        }).subscribe();
    }

    @Bean
    public ReactiveRedisMessageListenerContainer redisContainer() {
        return this.container;
    }
}
