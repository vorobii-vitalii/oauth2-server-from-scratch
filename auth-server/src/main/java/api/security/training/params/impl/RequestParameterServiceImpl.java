package api.security.training.params.impl;

import api.security.training.params.RequestParameter;
import api.security.training.params.RequestParameterService;
import io.javalin.http.Context;

public class RequestParameterServiceImpl implements RequestParameterService {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Context context, RequestParameter<T> parameter) {
		return (T) context.attribute(parameter.parameterName());
	}

	@Override
	public <T> void set(Context context, RequestParameter<T> parameter, T value) {
		context.attribute(parameter.parameterName(), value);
	}
}
