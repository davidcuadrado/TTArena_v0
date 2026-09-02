package org.ttarena.arena_map.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("map")
public record MapProperties(@Min(0) @Max(64) int maxRadius, @Min(1) int maxPerOwner) {
}
