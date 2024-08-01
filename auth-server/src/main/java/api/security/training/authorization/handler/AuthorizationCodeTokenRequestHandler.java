package api.security.training.authorization.handler;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import api.security.training.api.dto.AuthorizationCodeTokenRequest;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.ClientAuthenticationCodeRepository;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.authorization.domain.ClientRefreshToken;
import api.security.training.token.utils.ScopesParser;
import api.security.training.UUIDSupplier;
import api.security.training.token.AccessTokenCreator;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AuthorizationCodeTokenRequestHandler implements TokenRequestHandler {
	private static final String AUTHORIZATION_CODE = "authorization_code";

	private final ClientAuthenticationCodeRepository clientAuthenticationCodeRepository;
	private final UUIDSupplier uuidSupplier;
	private final ClientRefreshTokenRepository clientRefreshTokenRepository;
	private final Clock clock;
	private final AccessTokenCreator accessTokenCreator;

	@Override
	public boolean canHandleGrantType(String grantType) {
		return AUTHORIZATION_CODE.equals(grantType);
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		var authCodeTokenRequest = ctx.bodyAsClass(AuthorizationCodeTokenRequest.class);
		if (authCodeTokenRequest.code() == null) {
			log.warn("Code not provided");
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(List.of("Code is absent"));
			return;
		}
		var clientId = Objects.requireNonNull(ctx.basicAuthCredentials()).getUsername();
		log.info("All good. Client provided correct credentials...");
		var clientAuthCodeOpt = clientAuthenticationCodeRepository.findById(UUID.fromString(authCodeTokenRequest.code()));
		if (clientAuthCodeOpt.isEmpty()) {
			log.warn("There is no client authentication code {}", authCodeTokenRequest.code());
			ctx.status(HttpStatus.NOT_FOUND);
			ctx.json(List.of("Code not found"));
			return;
		}
		var clientAuthenticationCode = clientAuthCodeOpt.get();
		var clientRefreshToken = clientRefreshTokenRepository.save(ClientRefreshToken.builder()
				.clientId(UUID.fromString(clientId))
				.createdAt(Instant.now(clock))
				.refreshToken(uuidSupplier.createUUID())
				.scope(clientAuthenticationCode.scope())
				.username(clientAuthenticationCode.username())
				.build());
		var accessToken = accessTokenCreator.createToken(
				clientAuthenticationCode.username(),
				ScopesParser.parseAuthorizationScopes(clientAuthenticationCode.scope()).orElse(Arrays.asList(AuthorizationScope.values())));
		clientAuthenticationCodeRepository.delete(clientAuthenticationCode);
		ctx.json(Map.of(
				"access_token", accessToken,
				"refresh_token", clientRefreshToken.refreshToken().toString()
		));
		ctx.status(HttpStatus.OK);

		//		{
		//			"access_token":"2YotnFZFEjr1zCsicMWpAA",
		//				"token_type":"example",
		//				"expires_in":3600,
		//				"refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
		//				"example_parameter":"example_value"
		//		}

	}
}
