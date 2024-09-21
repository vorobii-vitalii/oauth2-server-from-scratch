package api.security.training.authorization.service;

import com.spencerwi.either.Result;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.dto.ClientCredentials;

public interface TokenRequestService {
	Result<TokenResponse> handleTokenRequest(TokenRequest tokenRequest, ClientCredentials clientCredentials);
}
