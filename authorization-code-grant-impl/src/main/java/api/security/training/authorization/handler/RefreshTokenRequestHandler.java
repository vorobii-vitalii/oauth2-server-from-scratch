package api.security.training.authorization.handler;

import java.util.UUID;

import com.spencerwi.either.Result;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.token.AccessTokenCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RefreshTokenRequestHandler implements TokenRequestHandler {
	private static final String REFRESH_TOKEN = "refresh_token";

	private final ClientRefreshTokenRepository clientRefreshTokenRepository;
	private final AccessTokenCreator accessTokenCreator;

	@Override
	public boolean canHandleGrantType(String grantType) {
		return REFRESH_TOKEN.equals(grantType);
	}

	@Override
	public Result<TokenResponse> handleTokenRequest(TokenRequest tokenRequest, String clientId) {
		// refresh_token, scope
		var refreshTokenStr = tokenRequest.refreshToken();
		if (refreshTokenStr == null) {
			log.warn("Refresh token not specified...");
			return Result.err(new IllegalArgumentException("Refresh token not specified"));
		}
		var foundClientRefreshToken = clientRefreshTokenRepository.findById(UUID.fromString(refreshTokenStr));
		if (foundClientRefreshToken.isEmpty()) {
			log.warn("Refresh token not found...");
			return Result.err(new IllegalArgumentException("Refresh token not found"));
		}
		var clientRefreshToken = foundClientRefreshToken.get();
		if (checkScopeNotWider(tokenRequest.scope(), clientRefreshToken.scope())) {
			// Calc scope and generate
		} else {
			log.warn("Attempt to request token with higher scope {} > {}", tokenRequest.scope(), clientRefreshToken.scope());
			return Result.err(new IllegalArgumentException("Invalid scope"));
		}
	}

	private boolean checkScopeNotWider(String requestedScope, String refreshTokenScope) {

	}

}
