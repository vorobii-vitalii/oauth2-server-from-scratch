package api.security.training.authorization;

import api.security.training.authorization.domain.AuthorizationRequest;

public interface AuthorizationRedirectStrategy {
	String computeAuthorizationRedirectURL(AuthorizationRequest authorizationRequest);
	boolean canHandleResponseType(String responseType);
}
