package org.ttarena.arena_auth.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.ttarena.arena_auth.dto.AuthenticatedUser;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUserPrincipal implements UserDetails {

    private final String userId;
    private final String username;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUserPrincipal(String userId, String username, List<GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.authorities = authorities;
    }

    public static AuthenticatedUserPrincipal of(AuthenticatedUser user) {
        List<GrantedAuthority> authorities = user.roles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();

        return new AuthenticatedUserPrincipal(user.userId(), user.username(), authorities);
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }
}
