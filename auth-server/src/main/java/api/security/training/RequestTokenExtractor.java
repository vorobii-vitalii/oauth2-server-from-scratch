package api.security.training;

import java.util.Optional;

import io.javalin.http.Context;

public interface RequestTokenExtractor {
	Optional<String> extractTokenFromRequest(Context context);
}
