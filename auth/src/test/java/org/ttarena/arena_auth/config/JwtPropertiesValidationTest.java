package org.ttarena.arena_auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Misconfiguration should stop the application at startup, not surface as a
 * confusing failure on the first login.
 */
class JwtPropertiesValidationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(EnableJwtProperties.class);

    @Test
    void aCompleteConfigurationBinds() {
        runner.withPropertyValues(
                        "ttarena.jwt.private-key=classpath:keys/dev-private.pem",
                        "ttarena.jwt.public-key=classpath:keys/dev-public.pem",
                        "ttarena.jwt.ttl-minutes=30")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(JwtProperties.class).ttlMinutes()).isEqualTo(30);
                });
    }

    @Test
    void aTtlOfZeroIsRefusedAtStartup() {
        runner.withPropertyValues(
                        "ttarena.jwt.private-key=classpath:keys/dev-private.pem",
                        "ttarena.jwt.public-key=classpath:keys/dev-public.pem",
                        "ttarena.jwt.ttl-minutes=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void aMissingKeyIsRefusedAtStartup() {
        runner.withPropertyValues(
                        "ttarena.jwt.public-key=classpath:keys/dev-public.pem",
                        "ttarena.jwt.ttl-minutes=30")
                .run(context -> assertThat(context).hasFailed());
    }

    @org.springframework.boot.context.properties.EnableConfigurationProperties(JwtProperties.class)
    static class EnableJwtProperties {
    }
}
