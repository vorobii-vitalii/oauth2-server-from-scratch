package api.security.training.authorization;

import com.spencerwi.either.Either;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.dto.TokenGenerationError;

public interface TokenRequestHandler {
	boolean canHandleGrantType(String grantType);
	Either<TokenResponse, TokenGenerationError> handleTokenRequest(TokenRequest tokenRequest, String clientId);
}
