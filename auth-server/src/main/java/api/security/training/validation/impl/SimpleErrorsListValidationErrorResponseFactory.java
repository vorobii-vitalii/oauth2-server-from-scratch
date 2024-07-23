package api.security.training.validation.impl;

import java.util.Set;

import api.security.training.validation.ValidationErrorResponseFactory;
import jakarta.validation.ConstraintViolation;

public class SimpleErrorsListValidationErrorResponseFactory implements ValidationErrorResponseFactory {
	@Override
	public <T> Object createErrorValidationResponse(Set<ConstraintViolation<T>> violations) {
		return violations.stream()
				.map(v -> v.getPropertyPath() + " -> " + v.getMessage())
				.toList();
	}
}
