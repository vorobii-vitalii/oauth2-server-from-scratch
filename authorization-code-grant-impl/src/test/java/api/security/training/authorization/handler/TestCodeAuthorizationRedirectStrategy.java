package api.security.training.authorization.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import api.security.training.authorization.dao.ClientAuthenticationCodeRepository;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.domain.ClientAuthenticationCode;
import api.security.training.authorization.utils.URIParametersAppender;
import api.security.training.token.dto.AuthorizationScope;

@ExtendWith(MockitoExtension.class)
class TestCodeAuthorizationRedirectStrategy {
	private static final UUID AUTH_REQUEST_ID = UUID.randomUUID();
	private static final UUID CLIENT_ID = UUID.randomUUID();
	private static final String STATE = "123";
	private static final String USERNAME = "user123";
	private static final URI REDIRECT_URL = URI.create("http://host/callback");
	private static final UUID GENERATED_UUID = UUID.randomUUID();

	@Mock
	ClientAuthenticationCodeRepository clientAuthenticationCodeRepository;

	@Mock
	URIParametersAppender uriParametersAppender;

	CodeAuthorizationRedirectStrategy authorizationRedirectStrategy;

	@BeforeEach
	void init() {
		authorizationRedirectStrategy = new CodeAuthorizationRedirectStrategy(
				clientAuthenticationCodeRepository,
				() -> GENERATED_UUID,
				uriParametersAppender
		);
	}

	@Test
	void computeAuthorizationRedirectURLGivenStateWasPreviouslyPassed() {
		var authorizationRequest = AuthorizationRequest.builder()
				.id(AUTH_REQUEST_ID)
				.clientId(CLIENT_ID)
				.scope(AuthorizationScope.READ_FIRST_NAME.getCode())
				.state(STATE)
				.username(USERNAME)
				.redirectURL(REDIRECT_URL)
				.build();
		when(uriParametersAppender.appendParameters(REDIRECT_URL, Map.of("code", GENERATED_UUID.toString(), "state", STATE)))
				.thenReturn(URI.create("http://host/callback?code=123&state=xyz"));
		assertThat(authorizationRedirectStrategy.computeAuthorizationRedirectURL(authorizationRequest))
				.isEqualTo(URI.create("http://host/callback?code=123&state=xyz"));
		verify(clientAuthenticationCodeRepository).save(ClientAuthenticationCode.builder()
				.code(GENERATED_UUID)
				.clientId(CLIENT_ID)
				.authorizationRequestId(AUTH_REQUEST_ID)
				.scope(AuthorizationScope.READ_FIRST_NAME.getCode())
				.state(STATE)
				.username(USERNAME)
				.build());
	}

	@Test
	void computeAuthorizationRedirectURLGivenStateWasPreviouslyNotPassed() {
		var authorizationRequest = AuthorizationRequest.builder()
				.id(AUTH_REQUEST_ID)
				.clientId(CLIENT_ID)
				.scope(AuthorizationScope.READ_FIRST_NAME.getCode())
				.username(USERNAME)
				.redirectURL(REDIRECT_URL)
				.build();
		when(uriParametersAppender.appendParameters(REDIRECT_URL, Map.of("code", GENERATED_UUID.toString())))
				.thenReturn(URI.create("http://host/callback?code=123"));
		assertThat(authorizationRedirectStrategy.computeAuthorizationRedirectURL(authorizationRequest))
				.isEqualTo(URI.create("http://host/callback?code=123"));
		verify(clientAuthenticationCodeRepository).save(ClientAuthenticationCode.builder()
				.code(GENERATED_UUID)
				.clientId(CLIENT_ID)
				.authorizationRequestId(AUTH_REQUEST_ID)
				.scope(AuthorizationScope.READ_FIRST_NAME.getCode())
				.username(USERNAME)
				.build());
	}
}
