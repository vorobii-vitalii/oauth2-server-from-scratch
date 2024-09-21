package api.security.training.authorization.utils.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import api.security.training.authorization.utils.URIParametersAppender;

class TestURIParametersAppenderImpl {

	URIParametersAppender uriParametersAppender = new URIParametersAppenderImpl();

	public static Stream<Arguments> appendParametersTestParams() {
		return Stream.of(
				Arguments.of(
						"http://host:80/endpoint",
						Map.of("x", "24"),
						"http://host:80/endpoint?x=24"
				),
				Arguments.of(
						"http://host:80/endpoint/",
						Map.of("x", "24"),
						"http://host:80/endpoint/?x=24"
				)
		);
	}

	@ParameterizedTest
	@MethodSource("appendParametersTestParams")
	void appendParameters(String originalURI, Map<String, String> params, String expectedResultURI) {
		assertThat(uriParametersAppender.appendParameters(URI.create(originalURI), params).toString())
				.isEqualTo(expectedResultURI);
	}
}
