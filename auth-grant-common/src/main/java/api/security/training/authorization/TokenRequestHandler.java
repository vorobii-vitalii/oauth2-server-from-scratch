package api.security.training.authorization;

import com.spencerwi.either.Result;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;

public interface TokenRequestHandler {
	boolean canHandleGrantType(String grantType);

	// TODO: maybe change to UUID
	Result<TokenResponse> handleTokenRequest(TokenRequest tokenRequest, String clientId);
}
