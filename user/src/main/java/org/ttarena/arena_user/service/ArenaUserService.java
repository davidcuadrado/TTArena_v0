package org.ttarena.arena_user.service;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.ttarena.arena_user.document.ArenaUserDocument;
import org.ttarena.arena_user.repository.ArenaUserRepository;
import reactor.core.publisher.Mono;

@Service
@Primary
public class ArenaUserService implements ReactiveUserDetailsService {

	private final ArenaUserRepository userRepository;

	public ArenaUserService(ArenaUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Mono<ArenaUserDocument> findByUsernameMono(Mono<String> monoUsername) {
		return monoUsername.flatMap(username -> userRepository.findByUsername(username)
        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Couldn't find any user with this username. "))));
	}

	@Override
	public Mono<UserDetails> findByUsername(String username){
		return userRepository.findByUsername(username)
				.map(user -> User.builder().username(user.getUsername()).password(user.getPassword())
						.roles(getRoles(user)).build())
				.switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
	}

	protected String[] getRoles(ArenaUserDocument user) {
		if (user.getRole() == null) {
			return new String[] { "USER" };
		}
		return user.getRole().split(",");
	}
}
