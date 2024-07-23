package api.security.training.validation;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

public interface ValidationErrorResponseFactory {
	<T> Object createErrorValidationResponse(Set<ConstraintViolation<T>> violations);
}
