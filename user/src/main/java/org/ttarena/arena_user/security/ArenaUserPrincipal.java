package org.ttarena.arena_user.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.ttarena.arena_user.document.ArenaUserDocument;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ArenaUserPrincipal implements UserDetails {

	private final String userId;
	private final String username;
	private final String password;
	private final List<GrantedAuthority> authorities;

	public ArenaUserPrincipal(String userId, String username, String password, List<GrantedAuthority> authorities) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}

	public static ArenaUserPrincipal of(ArenaUserDocument user) {
		String roles = user.getRole() == null ? "USER" : user.getRole();
		List<GrantedAuthority> authorities = Arrays.stream(roles.split(","))
				.map(String::trim)
				.filter(role -> !role.isEmpty())
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
				.map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
				.toList();

		return new ArenaUserPrincipal(user.getUserId(), user.getUsername(), user.getPassword(), authorities);
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
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}
}
