package org.ttarena.arena_user.service;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.ttarena.arena_user.document.ArenaUserDocument;
import org.ttarena.arena_user.dto.AuthenticationRequest;
import org.ttarena.arena_user.dto.RegisterUserRequest;
import org.ttarena.arena_user.exception.BadRequestException;
import org.ttarena.arena_user.repository.ArenaUserRepository;
import org.ttarena.arena_user.security.ArenaUserPrincipal;
import reactor.core.publisher.Mono;

@Service
@Primary
public class ArenaUserService implements ReactiveUserDetailsService {

	private final ArenaUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public ArenaUserService(ArenaUserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Mono<ArenaUserDocument> findByUsernameMono(Mono<String> monoUsername) {
		return monoUsername.flatMap(username -> userRepository.findByUsername(username)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFoundException("Couldn't find any user with this username. ")))));
	}

	@Override
	public Mono<UserDetails> findByUsername(String username) {
		return userRepository.findByUsername(username)
				.map(user -> (UserDetails) ArenaUserPrincipal.of(user))
				.switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFoundException(username))));
	}

	public Mono<ArenaUserDocument> register(RegisterUserRequest request) {
		return userRepository.findByUsername(request.username())
				.flatMap(existing -> Mono.<ArenaUserDocument>error(
						new BadRequestException("Username '" + request.username() + "' is already taken.")))
				.switchIfEmpty(Mono.defer(() -> userRepository.save(ArenaUserDocument.newUser(
						request.username(),
						request.email(),
						passwordEncoder.encode(request.password())))));
	}

	public Mono<ArenaUserDocument> authenticate(AuthenticationRequest request) {
		return userRepository.findByUsername(request.username())
				.filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
				.switchIfEmpty(Mono.defer(() -> Mono.error(new BadRequestException("Invalid username or password."))));
	}
}
