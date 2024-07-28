package api.security.training.authorization.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthorizationScope {
	READ_FIRST_NAME("read_first_name", "Read first name"),
	READ_LAST_NAME("read_last_name", "Read last name"),
	READ_EMAIL("read_email", "Read email address"),
	CHANGE_FIRST_NAME("change_first_name", "Change first name"),
	CHANGE_LAST_NAME("change_last_name", "Change last name"),
	APPROVE_AUTHORIZATION_REQUEST("approve_auth_request", "Approve authorization request");

	private static final Map<String, AuthorizationScope> SCOPE_MAP = Arrays.stream(values())
			.collect(Collectors.toMap(AuthorizationScope::getCode, v -> v));

	private final String code;
	private final String displayName;

	public static AuthorizationScope parse(String str) {
		return SCOPE_MAP.get(str);
	}
}
