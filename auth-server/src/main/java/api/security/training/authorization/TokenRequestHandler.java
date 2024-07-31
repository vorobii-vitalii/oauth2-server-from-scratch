package api.security.training.authorization;

import io.javalin.http.Handler;

public interface TokenRequestHandler extends Handler {
	boolean canHandleGrantType(String grantType);
}
