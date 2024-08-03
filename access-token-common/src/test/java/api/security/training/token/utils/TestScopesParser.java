package api.security.training.token.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import api.security.training.token.dto.AuthorizationScope;

class TestScopesParser {

	public static Stream<Arguments> validScopesArguments() {
		return Stream.of(
				Arguments.of(
						"read_first_name read_email", List.of(AuthorizationScope.READ_FIRST_NAME, AuthorizationScope.READ_EMAIL)
				),
				Arguments.of(
						"read_first_name    read_email", List.of(AuthorizationScope.READ_FIRST_NAME, AuthorizationScope.READ_EMAIL)
				),
				Arguments.of(
						" read_first_name    read_email  ", List.of(AuthorizationScope.READ_FIRST_NAME, AuthorizationScope.READ_EMAIL)
				),
				Arguments.of(
						"approve_auth_request", List.of(AuthorizationScope.APPROVE_AUTHORIZATION_REQUEST)
				)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"123", "read_first_name 123"})
	void givenInvalidScopes(String str) {
		assertThat(ScopesParser.parseAuthorizationScopes(str).isErr()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validScopesArguments")
	void givenValidScopes(String str, List<AuthorizationScope> expectedParsedScopes) {
		var result = ScopesParser.parseAuthorizationScopes(str);
		assertThat(result.isOk()).isTrue();
		assertThat(result.getResult()).isEqualTo(expectedParsedScopes);
	}

}