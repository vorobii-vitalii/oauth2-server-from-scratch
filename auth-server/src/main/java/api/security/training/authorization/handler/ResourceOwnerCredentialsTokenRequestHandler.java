package api.security.training.authorization.handler;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.ResourceOwnerCredentialsTokenRequest;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.authorization.domain.AuthorizationScope;
import api.security.training.authorization.domain.ClientRefreshToken;
import api.security.training.authorization.utils.ScopesParser;
import api.security.training.client_registration.UUIDSupplier;
import api.security.training.token.AccessTokenCreator;
import api.security.training.users.dao.UserRepository;
import api.security.training.users.password.PasswordService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ResourceOwnerCredentialsTokenRequestHandler implements TokenRequestHandler {
	private static final String PASSWORD_GRANT_TYPE = "password";

	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final AccessTokenCreator accessTokenCreator;
	private final UUIDSupplier uuidSupplier;
	private final ClientRefreshTokenRepository clientRefreshTokenRepository;
	private final Clock clock;

	@Override
	public boolean canHandleGrantType(String grantType) {
		return PASSWORD_GRANT_TYPE.equals(grantType);
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		var tokenRequest = ctx.bodyAsClass(ResourceOwnerCredentialsTokenRequest.class);
		var username = tokenRequest.username();
		var foundUser = userRepository.findByUsername(username);
		var clientId = Objects.requireNonNull(ctx.basicAuthCredentials()).getUsername();
		if (foundUser.isPresent()) {
			log.info("User found. Verifying password...");
			var actualPasswordHash = foundUser.get().password();
			if (passwordService.isPasswordCorrect(actualPasswordHash, tokenRequest.password())) {
				log.info("Password is correct!");
				var authorizationScopes = ScopesParser.parseAuthorizationScopes(tokenRequest.scope())
						.orElseGet(() -> Arrays.asList(AuthorizationScope.values()));
				var clientRefreshToken = clientRefreshTokenRepository.save(ClientRefreshToken.builder()
						.clientId(UUID.fromString(clientId))
						.createdAt(Instant.now(clock))
						.refreshToken(uuidSupplier.createUUID())
						.scope(tokenRequest.scope())
						.username(username)
						.build());
				var accessToken = accessTokenCreator.createToken(username, authorizationScopes);
				ctx.json(Map.of(
						"access_token", accessToken,
						"refresh_token", clientRefreshToken.refreshToken().toString()
				));
				ctx.status(HttpStatus.OK);
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
	}
}
