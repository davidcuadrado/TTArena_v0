package org.ttarena.arena_user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Bounds how long the driver waits for a MongoDB that is not there.
 *
 * <p>Its default is to keep looking for a server for 30 seconds before
 * admitting it cannot find one - longer than any caller waits, including
 * {@code /actuator/health}, whose whole job is to answer promptly. A health
 * endpoint that outlives the probe calling it reports nothing at all, which is
 * worse than one that says DOWN.
 *
 * <p>Set here rather than in the connection string because {@code MONGODB_URI}
 * is meant to be replaced per environment, and a timeout carried inside the URI
 * is silently lost the moment somebody supplies their own.
 */
@Configuration
public class MongoTimeoutsConfig {

	private final Duration serverSelectionTimeout;

	private final Duration connectTimeout;

	public MongoTimeoutsConfig(
			@Value("${ttarena.mongo.server-selection-timeout:3s}") Duration serverSelectionTimeout,
			@Value("${ttarena.mongo.connect-timeout:2s}") Duration connectTimeout) {
		this.serverSelectionTimeout = serverSelectionTimeout;
		this.connectTimeout = connectTimeout;
	}

	@Bean
	public MongoClientSettingsBuilderCustomizer mongoTimeoutsCustomizer() {
		return builder -> builder
				.applyToClusterSettings(cluster -> cluster
						.serverSelectionTimeout(serverSelectionTimeout.toMillis(), TimeUnit.MILLISECONDS))
				.applyToSocketSettings(socket -> socket
						.connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS));
	}
}
