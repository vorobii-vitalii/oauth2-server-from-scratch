package api.security.training.client_registration;

import java.util.Optional;

import io.javalin.http.Context;

public class CookieRequestTokenExtractor implements RequestTokenExtractor {
	private static final String SESSION_COOKIE = "Session";

	@Override
	public Optional<String> extractTokenFromRequest(Context context) {
		return Optional.ofNullable(context.cookie(SESSION_COOKIE));
	}
}
