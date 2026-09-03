package org.ttarena.arena_user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.ttarena.arena_user.document.ArenaUserDocument;
import org.ttarena.arena_user.repository.ArenaUserRepository;
import org.ttarena.arena_user.service.JwtService;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Guards the wiring rather than the logic: that the public paths are reachable
 * without a token, that everything else is not, that a token gets you in, and
 * that a browser preflight is answered exactly once.
 *
 * <p>Every one of these covers something that was broken. The chain routed paths
 * no controller serves while leaving the actuator behind authentication; the
 * springdoc paths sat behind a role registration never grants; the JWT filter
 * was registered twice, and given no token at all it completed the exchange
 * without ever calling the chain. These are the tests that would have caught it.
 */
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				// The health endpoint asks every contributor, and this module has
				// two that need a server: MongoDB and Redis. Behind a test there is
				// neither, and neither fails fast - Mongo's driver waits out a 30
				// second server selection, and Lettuce queues the ping while it
				// tries to reconnect rather than refusing it - so the endpoint took
				// longer to answer than any caller would wait. What is asserted
				// here is that security lets the request reach the endpoint, not
				// what the endpoint then says about a database, so the contributors
				// are switched off and it answers immediately.
				"management.health.defaults.enabled=false",
				// Belt and braces: anything else in this test that reaches for
				// MongoDB gives up in milliseconds rather than in half a minute.
				"spring.data.mongodb.uri="
						+ "mongodb://localhost:27017/user-db?serverSelectionTimeoutMS=200&connectTimeoutMS=200"
		})
class UserSecurityIntegrationTest {

	private static final String TOKEN = "a-token-the-mocked-service-accepts";
	private static final String ORIGIN = "http://localhost:3000";

	@Value("${local.server.port}")
	private int port;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private ArenaUserRepository userRepository;

	private WebTestClient webTestClient;

	/**
	 * Bound to a real server rather than to the application context: CORS is
	 * partly handled by the HTTP layer, and a context-bound client does not
	 * reproduce it faithfully.
	 */
	@BeforeEach
	void bindToServer() {
		webTestClient = WebTestClient.bindToServer()
				.baseUrl("http://localhost:" + port)
				// Generous, because springdoc builds its document on first request.
				.responseTimeout(Duration.ofSeconds(15))
				.build();
	}

	private void givenTheTokenBelongsTo(String username) {
		ArenaUserDocument account = ArenaUserDocument.builder()
				.userId("user-1")
				.username(username)
				.email(username + "@ttarena.org")
				.password("irrelevant")
				.role("USER")
				.build();

		when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn(Mono.just(username));
		when(userRepository.findByUsername(username)).thenReturn(Mono.just(account));
	}

	private int statusOf(String path) {
		return webTestClient.get().uri(path)
				.exchange()
				.returnResult(Void.class)
				.getStatus()
				.value();
	}

	@Test
	void registrationIsReachableWithoutAToken() {
		when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());
		when(userRepository.save(any(ArenaUserDocument.class)))
				.thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

		webTestClient.post().uri("/user/register")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"username":"alice","email":"alice@ttarena.org","password":"password123"}""")
				.exchange()
				.expectStatus().isCreated();
	}

	@Test
	void everythingElseNeedsAToken() {
		webTestClient.get().uri("/user/home").exchange().expectStatus().isUnauthorized();
	}

	/**
	 * The filter used to answer 401 itself, and with no token at all it returned
	 * an empty sequence without calling the chain - indistinguishable from a
	 * completed response, so the caller got 200 and an empty body. Rejection is
	 * the authorization filter's job, and it is the only one doing it now.
	 */
	@Test
	void anUnreadableTokenIsRejectedByAuthorizationRatherThanByTheFilter() {
		when(jwtService.validateAndExtractUsername(anyString()))
				.thenReturn(Mono.error(new IllegalArgumentException("Invalid JWT token")));

		webTestClient.get().uri("/user/home")
				.header(HttpHeaders.AUTHORIZATION, "Bearer nonsense")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void aValidTokenIsServed() {
		givenTheTokenBelongsTo("alice");

		webTestClient.get().uri("/user/home")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
				.exchange()
				.expectStatus().isOk();
	}

	/**
	 * These three assert "not a rejection" rather than 200: with no MongoDB
	 * behind the test the health endpoint honestly reports DOWN, and springdoc
	 * serves its document per group, so the status is not the interesting part.
	 * Whether security let the request reach the endpoint at all is - and on this
	 * service alone, it did not.
	 */
	@Test
	void theActuatorHealthEndpointNeedsNoToken() {
		// Reachable, not healthy: the contributors are off, see the class header.
		assertThat(statusOf("/actuator/health")).isNotIn(401, 403);
	}

	@Test
	void theOpenApiDocumentNeedsNoToken() {
		assertThat(statusOf("/v3/api-docs")).isNotIn(401, 403);
	}

	@Test
	void theSwaggerUiNeedsNoToken() {
		assertThat(statusOf("/swagger-ui.html")).isNotIn(401, 403);
	}

	/**
	 * One header, not two. CORS is configured at the WebFlux level only; adding
	 * it to the security chain as well produces a duplicate
	 * {@code Access-Control-Allow-Origin}, which browsers reject outright.
	 */
	@Test
	void aPreflightIsAnsweredWithExactlyOneAllowOriginHeader() {
		HttpHeaders headers = webTestClient.options().uri("/user/register")
				.header(HttpHeaders.ORIGIN, ORIGIN)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
				.exchange()
				.returnResult(Void.class)
				.getResponseHeaders();

		assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).containsExactly(ORIGIN);
	}
}
