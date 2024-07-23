package api.security.training.validation;

import org.jetbrains.annotations.NotNull;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import jakarta.validation.Validator;

public class ValidatingBodyHandler<T> implements Handler {
	private final Validator validator;
	private final ValidationErrorResponseFactory validationErrorResponseFactory;
	private final Handler delegate;
	private final Class<T> bodyClass;

	public ValidatingBodyHandler(
			Validator validator,
			ValidationErrorResponseFactory validationErrorResponseFactory,
			Handler delegate,
			Class<T> bodyClass
	) {
		this.validator = validator;
		this.validationErrorResponseFactory = validationErrorResponseFactory;
		this.delegate = delegate;
		this.bodyClass = bodyClass;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		// TODO: Investigate how avoid deserialization twice. Maybe cache by bodyClass?
		T body = ctx.bodyAsClass(bodyClass);
		var violations = validator.validate(body);
		if (violations.isEmpty()) {
			delegate.handle(ctx);
		} else {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(validationErrorResponseFactory.createErrorValidationResponse(violations));
		}
	}
}
