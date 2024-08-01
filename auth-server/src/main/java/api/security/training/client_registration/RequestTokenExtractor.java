package api.security.training.client_registration;

import java.util.Optional;

import io.javalin.http.Context;

public interface RequestTokenExtractor {
	Optional<String> extractTokenFromRequest(Context context);
}
