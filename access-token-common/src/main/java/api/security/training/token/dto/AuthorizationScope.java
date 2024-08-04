package api.security.training.token.dto;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthorizationScope {
	READ_FIRST_NAME("read_first_name", "Read first name"),
	READ_LAST_NAME("read_last_name", "Read last name"),
	READ_PHONE_NUMBER("read_phone_number", "Read phone number"),
	CHANGE_FIRST_NAME("change_first_name", "Change first name"),
	CHANGE_LAST_NAME("change_last_name", "Change last name");

	private static final Map<String, AuthorizationScope> SCOPE_MAP = Arrays.stream(values())
			.collect(Collectors.toMap(AuthorizationScope::getCode, v -> v));

	private final String code;
	private final String displayName;

	public static Optional<AuthorizationScope> parse(String str) {
		return Optional.ofNullable(SCOPE_MAP.get(str));
	}
}
