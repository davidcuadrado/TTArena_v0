package org.ttarena.arena_auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.ttarena.arena_auth.dto.AuthenticatedUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserPrincipalTest {

    private AuthenticatedUserPrincipal principalWithRoles(List<String> roles) {
        return AuthenticatedUserPrincipal.of(
                new AuthenticatedUser("user-1", "alice", "alice@ttarena.org", roles));
    }

    @Test
    void rolesArePrefixedForSpringSecurity() {
        assertThat(principalWithRoles(List.of("USER", "ADMIN")).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    /**
     * The user service stores roles unprefixed today, but if that ever changes
     * the prefix must not be applied twice.
     */
    @Test
    void anAlreadyPrefixedRoleIsNotPrefixedAgain() {
        assertThat(principalWithRoles(List.of("ROLE_USER")).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void theUsernameAndAccountIdAreKeptApart() {
        AuthenticatedUserPrincipal principal = principalWithRoles(List.of("USER"));

        assertThat(principal.getUsername()).isEqualTo("alice");
        assertThat(principal.getUserId()).isEqualTo("user-1");
    }

    @Test
    void noPasswordIsCarried() {
        assertThat(principalWithRoles(List.of("USER")).getPassword()).isEmpty();
    }
}
