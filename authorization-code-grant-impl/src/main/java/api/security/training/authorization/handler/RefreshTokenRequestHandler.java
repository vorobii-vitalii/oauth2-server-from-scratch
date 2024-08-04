package api.security.training.authorization.handler;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.spencerwi.either.Result;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.token.utils.ScopesParser;
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
		if (!Objects.equals(clientRefreshToken.clientId(), UUID.fromString(clientId))) {
			log.warn("Refresh token is not associated with this client...");
			return Result.err(new IllegalArgumentException("Refresh token not for you"));
		}
		var refreshTokenScopes = parseScopes(clientRefreshToken.scope());
		var requestedScopes = tokenRequest.scope() == null
				? refreshTokenScopes
				: ScopesParser.parseAuthorizationScopes(tokenRequest.scope()).getResult();
		if (checkScopeNotWider(refreshTokenScopes, requestedScopes)) {
			var newAccessToken = accessTokenCreator.createToken(clientRefreshToken.username(), requestedScopes);
			return Result.ok(TokenResponse.builder().accessToken(newAccessToken).build());
		} else {
			log.warn("Attempt to request token with higher scope {} > {}", tokenRequest.scope(), clientRefreshToken.scope());
			return Result.err(new IllegalArgumentException("Invalid scope"));
		}
	}

	private boolean checkScopeNotWider(Collection<AuthorizationScope> refreshTokenScopes, Collection<AuthorizationScope> requestedScopes) {
		for (var requestedScope : requestedScopes) {
			if (!refreshTokenScopes.contains(requestedScope)) {
				log.warn("Scope {} not present in {}", requestedScope, refreshTokenScopes);
				return false;
			}
		}
		return true;
	}

	private List<AuthorizationScope> parseScopes(String scopes) {
		return scopes == null ? List.of(AuthorizationScope.values()) : ScopesParser.parseAuthorizationScopes(scopes).getResult();
	}

}
