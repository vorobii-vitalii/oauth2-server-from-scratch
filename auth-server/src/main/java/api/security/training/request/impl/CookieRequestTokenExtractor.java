package api.security.training.request.impl;

import java.util.Optional;

import api.security.training.request.RequestTokenExtractor;
import io.javalin.http.Context;

public class CookieRequestTokenExtractor implements RequestTokenExtractor {
	private static final String SESSION_COOKIE = "Session";

	@Override
	public Optional<String> extractTokenFromRequest(Context context) {
		return Optional.ofNullable(context.cookie(SESSION_COOKIE));
	}
}
