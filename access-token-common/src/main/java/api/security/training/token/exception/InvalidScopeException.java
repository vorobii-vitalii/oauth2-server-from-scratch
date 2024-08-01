package api.security.training.token.exception;

public class InvalidScopeException extends Exception {
	public InvalidScopeException(String errorMsg) {
		super(errorMsg);
	}
}
