package api.security.training.token.impl;

import java.util.Optional;

import api.security.training.token.RequestTokenExtractor;
import io.javalin.http.Context;

public class CookieRequestTokenExtractor implements RequestTokenExtractor {
	private static final String SESSION_COOKIE = "Session";

	@Override
	public Optional<String> extractTokenFromRequest(Context context) {
		return Optional.ofNullable(context.cookie(SESSION_COOKIE));
	}
}
