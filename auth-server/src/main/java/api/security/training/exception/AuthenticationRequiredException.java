package api.security.training.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AuthenticationRequiredException extends RuntimeException {
	private final String redirectTo;
}
