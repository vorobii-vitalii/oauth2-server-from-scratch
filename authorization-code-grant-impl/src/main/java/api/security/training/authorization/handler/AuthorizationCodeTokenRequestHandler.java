package api.security.training.authorization.handler;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.spencerwi.either.Result;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.ClientAuthenticationCodeRepository;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.authorization.domain.ClientRefreshToken;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.token.utils.ScopesParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AuthorizationCodeTokenRequestHandler implements TokenRequestHandler {
	private static final String AUTHORIZATION_CODE = "authorization_code";

	private final ClientAuthenticationCodeRepository clientAuthenticationCodeRepository;
	private final Supplier<UUID> uuidSupplier;
	private final ClientRefreshTokenRepository clientRefreshTokenRepository;
	private final Clock clock;
	private final AccessTokenCreator accessTokenCreator;

	@Override
	public boolean canHandleGrantType(String grantType) {
		return AUTHORIZATION_CODE.equals(grantType);
	}

	@SneakyThrows
	@Override
	public Result<TokenResponse> handleTokenRequest(TokenRequest tokenRequest, String clientId) {
		if (tokenRequest.code() == null) {
			log.warn("Code not provided");
			return Result.err(new IllegalArgumentException("Code is absent"));
		}
		log.info("All good. Client provided correct credentials...");
		var clientAuthCodeOpt = clientAuthenticationCodeRepository.findById(UUID.fromString(tokenRequest.code()));
		if (clientAuthCodeOpt.isEmpty()) {
			log.warn("There is no client authentication code {}", tokenRequest.code());
			return Result.err(new IllegalArgumentException("Code not found"));
		}
		var clientAuthenticationCode = clientAuthCodeOpt.get();
		var clientRefreshToken = clientRefreshTokenRepository.save(ClientRefreshToken.builder()
				.clientId(UUID.fromString(clientId))
				.createdAt(Instant.now(clock))
				.refreshToken(uuidSupplier.get())
				.scope(clientAuthenticationCode.scope())
				.username(clientAuthenticationCode.username())
				.build());
		String accessToken = accessTokenCreator.createToken(
				clientAuthenticationCode.username(),
				clientAuthenticationCode.scope() == null
						? List.of(AuthorizationScope.values())
						: ScopesParser.parseAuthorizationScopes(clientAuthenticationCode.scope()).getResult());
		clientAuthenticationCodeRepository.delete(clientAuthenticationCode);
		return Result.ok(TokenResponse.builder()
				.accessToken(accessToken)
				.refreshToken(clientRefreshToken.refreshToken().toString())
				.build());
	}
}
