package pi.security.training;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

import com.spencerwi.either.Either;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.authorization.domain.ClientRefreshToken;
import api.security.training.authorization.dto.ClientCredentials;
import api.security.training.authorization.dto.TokenGenerationError;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.token.utils.ScopesParser;
import api.security.training.users.login.service.UserCredentialsChecker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ResourceOwnerCredentialsTokenRequestHandler implements TokenRequestHandler {
	private static final String PASSWORD_GRANT_TYPE = "password";

	private final UserCredentialsChecker userCredentialsChecker;
	private final AccessTokenCreator accessTokenCreator;
	private final Supplier<UUID> uuidSupplier;
	private final ClientRefreshTokenRepository clientRefreshTokenRepository;
	private final Clock clock;

	@Override
	public boolean canHandleGrantType(String grantType) {
		return PASSWORD_GRANT_TYPE.equals(grantType);
	}

	@SneakyThrows
	@Override
	public Either<TokenResponse, TokenGenerationError> handleTokenRequest(TokenRequest tokenRequest, ClientCredentials clientCredentials) {
		var username = tokenRequest.username();
		boolean areCredentialsCorrect = userCredentialsChecker.areCredentialsCorrect(username, tokenRequest.password());
		if (areCredentialsCorrect) {
			log.info("Password is correct!");
			var authorizationScopes = ScopesParser.parseAuthorizationScopes(tokenRequest.scope())
					.orElseGet(() -> Arrays.asList(AuthorizationScope.values()));
			var clientRefreshToken = clientRefreshTokenRepository.save(ClientRefreshToken.builder()
					.clientId(UUID.fromString(clientCredentials.clientId()))
					.createdAt(Instant.now(clock))
					.refreshToken(uuidSupplier.get())
					.scope(tokenRequest.scope())
					.username(username)
					.build());
			var accessToken = accessTokenCreator.createToken(username, authorizationScopes);
			return Either.left(TokenResponse.builder()
					.accessToken(accessToken)
					.refreshToken(clientRefreshToken.refreshToken().toString())
					.build());
		}
		log.warn("Invalid credentials");
		return Either.right(new TokenGenerationError("Invalid credentials"));
	}
}
