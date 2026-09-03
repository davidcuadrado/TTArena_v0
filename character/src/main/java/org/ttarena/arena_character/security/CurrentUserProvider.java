package org.ttarena.arena_character.security;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.ttarena.arena_character.exception.ForbiddenException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CurrentUserProvider {

    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLES_CLAIM = "roles";

    public Mono<CurrentUser> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(CurrentUserProvider::fromJwt)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ForbiddenException("No authenticated user on this request."))));
    }

    private static CurrentUser fromJwt(Jwt jwt) {
        String userId = jwt.getClaimAsString(USER_ID_CLAIM);
        if (userId == null || userId.isBlank()) {
            throw new ForbiddenException("Token carries no userId claim.");
        }

        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        return new CurrentUser(userId, jwt.getSubject(), roles == null ? List.of() : roles);
    }
}
