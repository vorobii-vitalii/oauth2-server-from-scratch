package api.security.training.request;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestParameters {
	public static final RequestParameter<String> USERNAME = new RequestParameter<>("username");
	public static final RequestParameter<Boolean> IS_AUTH_SESSION_ACTIVE = new RequestParameter<>("isAuthSessionActive");
}
