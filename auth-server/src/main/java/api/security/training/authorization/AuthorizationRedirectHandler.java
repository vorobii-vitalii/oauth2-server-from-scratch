package api.security.training.authorization;

import api.security.training.authorization.domain.AuthorizationRequest;
import reactor.core.publisher.Mono;

public interface AuthorizationRedirectHandler {
	// Returns redirect URL
	String handleAuthorizationRedirect(AuthorizationRequest authorizationRequest);
	boolean canHandleResponseType(String responseType);
}
