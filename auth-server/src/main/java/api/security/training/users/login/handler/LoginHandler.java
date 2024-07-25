package api.security.training.users.login.handler;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Query;

import api.security.training.client_registration.domain.ClientRegistration;
import api.security.training.token.TokenCreator;
import api.security.training.users.domain.User;
import api.security.training.users.login.dto.UserLoginRequest;
import api.security.training.users.login.redirect.AuthorizationRedirectURLFactory;
import api.security.training.users.password.PasswordService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class LoginHandler implements Handler {
	private final R2dbcEntityOperations entityOperations;
	private final PasswordService passwordService;
	private final TokenCreator tokenCreator;
	private final long sessionCookieExpirationMs;

	@Override
	public void handle(@NotNull Context ctx) {
		var userLoginRequest = ctx.bodyAsClass(UserLoginRequest.class);
		log.info("Handling user login request = {}", userLoginRequest);
		var username = userLoginRequest.username();
		//		Optional<AuthorizationRedirectURLFactory> authorizationRedirectURLFactory = authorizationRedirectURLFactories.stream()
		//				.filter(v -> Objects.equals(v.grantType(), userLoginRequest.grantType()))
		//				.findFirst();
		//		if (authorizationRedirectURLFactory.isEmpty()) {
		//			log.warn("Grant type = {} not supported", userLoginRequest.grantType());
		//			ctx.status(HttpStatus.BAD_REQUEST);
		//			ctx.json(List.of("Such grant type not supported"));
		//			return;
		//		}

		ctx.future(
				entityOperations.selectOne(queryUserByUsername(username), User.class)
						.map(Optional::ofNullable)
						.switchIfEmpty(Mono.just(Optional.empty()))
						.doOnNext(foundUser -> {
							if (foundUser.isPresent()) {
								log.info("User found. Verifying password...");
								var actualPasswordHash = foundUser.get().password();
								if (passwordService.isPasswordCorrect(actualPasswordHash, userLoginRequest.password())) {
									log.info("Password is correct! Performing redirect!");
									var redirectURL = userLoginRequest.redirectToOnSuccess();
									// Generate JWT, sign it and set in cookie. STRICT TRUE, HTTP ONLY
									ctx.cookie("Session", tokenCreator.createToken(username, Map.of()), (int) sessionCookieExpirationMs);
									ctx.redirect(redirectURL, HttpStatus.FOUND);
								} else {
									log.warn("Password is wrong...");
									ctx.status(HttpStatus.UNAUTHORIZED);
									ctx.json(List.of("Wrong password"));
								}
							} else {
								log.warn("User with such username {} not found...", username);
								ctx.status(HttpStatus.BAD_REQUEST);
								ctx.json(List.of("Such user not found"));
							}
						})::toFuture
		);

		//		ctx.future(entityOperations.selectOne(queryClientRegistrationByID(userLoginRequest.clientId()), ClientRegistration.class)
		//				.map(Optional::ofNullable)
		//				.switchIfEmpty(Mono.just(Optional.empty()))
		//				.flatMap(clientRegistrationOpt -> {
		//					if (clientRegistrationOpt.isEmpty()) {
		//						log.warn("Client with id = {} not found...", userLoginRequest.clientId());
		//						return Mono.empty();
		//					}
		//
		//				})::toFuture);
	}

	private @NotNull Query queryClientRegistrationByID(UUID clientId) {
		return query(where(ClientRegistration.CLIENT_ID).is(clientId));
	}

	private @NotNull Query queryUserByUsername(String username) {
		return query(where(User.USER_ID).is(username));
	}

}
