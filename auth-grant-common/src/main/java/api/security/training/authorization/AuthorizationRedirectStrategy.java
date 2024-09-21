package api.security.training.authorization;

import java.net.URI;

import api.security.training.authorization.domain.AuthorizationRequest;

public interface AuthorizationRedirectStrategy {
	URI computeAuthorizationRedirectURL(AuthorizationRequest authorizationRequest);
	boolean canHandleResponseType(String responseType);
}
