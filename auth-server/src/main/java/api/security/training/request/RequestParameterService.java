package api.security.training.request;

import io.javalin.http.Context;

public interface RequestParameterService {
	<T> T get(Context context, RequestParameter<T> parameter);
	<T> void set(Context context, RequestParameter<T> parameter, T value);
}
