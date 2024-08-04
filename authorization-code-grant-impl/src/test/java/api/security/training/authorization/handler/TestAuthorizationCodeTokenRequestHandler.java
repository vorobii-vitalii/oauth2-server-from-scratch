package api.security.training.authorization.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.dao.ClientAuthenticationCodeRepository;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.authorization.domain.ClientAuthenticationCode;
import api.security.training.authorization.domain.ClientRefreshToken;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;

@ExtendWith(MockitoExtension.class)
class TestAuthorizationCodeTokenRequestHandler {
	private static final String USERNAME = "myuser";
	private static final String SCOPE = "read_first_name";
	private static final String ACCESS_TOKEN = "accessToken123";
	private static final UUID AUTHENTICATION_CODE = UUID.randomUUID();
	private static final UUID GENERATED_UUID = UUID.randomUUID();
	private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
	private static final UUID CLIENT_ID = UUID.randomUUID();

	@Mock
	ClientAuthenticationCodeRepository clientAuthenticationCodeRepository;

	@Mock
	ClientRefreshTokenRepository clientRefreshTokenRepository;

	@Mock
	AccessTokenCreator accessTokenCreator;

	AuthorizationCodeTokenRequestHandler tokenRequestHandler;

	@BeforeEach
	void init() {
		tokenRequestHandler = new AuthorizationCodeTokenRequestHandler(
				clientAuthenticationCodeRepository,
				() -> GENERATED_UUID,
				clientRefreshTokenRepository,
				CLOCK,
				accessTokenCreator
		);
	}

	@Test
	void shouldHandleAuthorizationCodeGrantType() {
		assertThat(tokenRequestHandler.canHandleGrantType("authorization_code")).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"password", "refresh_token"})
	void shouldNotHandleAnyOtherGrantType(String grantType) {
		assertThat(tokenRequestHandler.canHandleGrantType(grantType)).isFalse();
	}

	@Test
	void handleTokenRequestGivenCodeIsNull() {
		TokenRequest tokenRequest = TokenRequest.builder()
				.build();
		var tokenResponseResult = tokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void handleTokenRequestGivenAuthenticationCodeNotFound() {
		TokenRequest tokenRequest = TokenRequest.builder()
				.code(AUTHENTICATION_CODE.toString())
				.build();
		when(clientAuthenticationCodeRepository.findById(AUTHENTICATION_CODE))
				.thenReturn(Optional.empty());
		var tokenResponseResult = tokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void handleTokenRequestGivenClientIdInAuthenticationCodeIsDifferent() {
		TokenRequest tokenRequest = TokenRequest.builder()
				.code(AUTHENTICATION_CODE.toString())
				.build();
		when(clientAuthenticationCodeRepository.findById(AUTHENTICATION_CODE))
				.thenReturn(Optional.of(ClientAuthenticationCode.builder()
						.code(AUTHENTICATION_CODE)
						.clientId(UUID.randomUUID())
						.build()));
		var tokenResponseResult = tokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void handleTokenRequestGivenClientIdInAuthenticationCodeIsSame() {
		TokenRequest tokenRequest = TokenRequest.builder()
				.code(AUTHENTICATION_CODE.toString())
				.build();
		when(clientAuthenticationCodeRepository.findById(AUTHENTICATION_CODE))
				.thenReturn(Optional.of(ClientAuthenticationCode.builder()
						.code(AUTHENTICATION_CODE)
						.clientId(CLIENT_ID)
						.username(USERNAME)
						.scope(SCOPE)
						.build()));
		when(accessTokenCreator.createToken(USERNAME, List.of(AuthorizationScope.READ_FIRST_NAME)))
				.thenReturn(ACCESS_TOKEN);
		var tokenResponseResult = tokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.getResult()).isEqualTo(
				TokenResponse.builder()
						.accessToken(ACCESS_TOKEN)
						.refreshToken(GENERATED_UUID.toString())
						.build());
		verify(clientRefreshTokenRepository).save(ClientRefreshToken.builder()
				.clientId(CLIENT_ID)
				.createdAt(Instant.now(CLOCK))
				.refreshToken(GENERATED_UUID)
				.scope(SCOPE)
				.username(USERNAME)
				.build());
	}

}
