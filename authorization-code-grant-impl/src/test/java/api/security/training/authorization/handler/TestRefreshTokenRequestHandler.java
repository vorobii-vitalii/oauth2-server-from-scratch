package api.security.training.authorization.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import api.security.training.authorization.domain.ClientRefreshToken;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.dto.AuthorizationScope;

@ExtendWith(MockitoExtension.class)
class TestRefreshTokenRequestHandler {
	private static final String USERNAME = "myUser";
	private static final String ACCESS_TOKEN = "accessToken123";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final UUID CLIENT_ID = UUID.randomUUID();
	private static final UUID REFRESH_TOKEN_ID = UUID.randomUUID();

	@Mock
	ClientRefreshTokenRepository clientRefreshTokenRepository;

	@Mock
	AccessTokenCreator accessTokenCreator;

	@InjectMocks
	RefreshTokenRequestHandler refreshTokenRequestHandler;

	@Test
	void canHandleRefreshTokenGrantType() {
		assertThat(refreshTokenRequestHandler.canHandleGrantType(REFRESH_TOKEN)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"token", "code"})
	void shouldNotHandleOtherGrantTypes(String grantType) {
		assertThat(refreshTokenRequestHandler.canHandleGrantType(grantType)).isFalse();
	}

	@Test
	void givenRefreshTokenNotProvided() {
		var tokenRequest = TokenRequest.builder()
				.grantType(REFRESH_TOKEN)
				.build();
		var tokenResponseResult = refreshTokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void givenRefreshTokenNotFoundInDB() {
		var tokenRequest = TokenRequest.builder()
				.grantType(REFRESH_TOKEN)
				.refreshToken(REFRESH_TOKEN_ID.toString())
				.build();
		when(clientRefreshTokenRepository.findById(REFRESH_TOKEN_ID)).thenReturn(Optional.empty());
		var tokenResponseResult = refreshTokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void givenAttemptToGetAccessTokenCreatedForDifferentClient() {
		var tokenRequest = TokenRequest.builder()
				.grantType(REFRESH_TOKEN)
				.refreshToken(REFRESH_TOKEN_ID.toString())
				.build();
		when(clientRefreshTokenRepository.findById(REFRESH_TOKEN_ID))
				.thenReturn(Optional.of(ClientRefreshToken.builder()
						.clientId(UUID.randomUUID())
						.build()));
		var tokenResponseResult = refreshTokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void givenAccessTokenRequestWithHigherScope() {
		var tokenRequest = TokenRequest.builder()
				.grantType(REFRESH_TOKEN)
				.refreshToken(REFRESH_TOKEN_ID.toString())
				.scope(AuthorizationScope.READ_PHONE_NUMBER.getCode() + " " + AuthorizationScope.READ_FIRST_NAME.getCode())
				.build();
		when(clientRefreshTokenRepository.findById(REFRESH_TOKEN_ID))
				.thenReturn(Optional.of(ClientRefreshToken.builder()
						.clientId(CLIENT_ID)
						.scope(AuthorizationScope.READ_PHONE_NUMBER.getCode())
						.build()));
		var tokenResponseResult = refreshTokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void givenAccessTokenRequestWithSubsetOfScopesAssociatedWithRefreshToken() {
		var tokenRequest = TokenRequest.builder()
				.grantType(REFRESH_TOKEN)
				.refreshToken(REFRESH_TOKEN_ID.toString())
				.scope(AuthorizationScope.READ_PHONE_NUMBER.getCode())
				.build();
		when(clientRefreshTokenRepository.findById(REFRESH_TOKEN_ID))
				.thenReturn(Optional.of(ClientRefreshToken.builder()
						.clientId(CLIENT_ID)
						.username(USERNAME)
						.scope(AuthorizationScope.READ_PHONE_NUMBER.getCode() + " " + AuthorizationScope.READ_FIRST_NAME.getCode())
						.build()));
		when(accessTokenCreator.createToken(USERNAME, List.of(AuthorizationScope.READ_PHONE_NUMBER)))
				.thenReturn(ACCESS_TOKEN);
		var tokenResponseResult = refreshTokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.getResult()).isEqualTo(TokenResponse.builder().accessToken(ACCESS_TOKEN).build());
	}

	@Test
	void givenAccessTokenRequestWithNoScope() {
		var tokenRequest = TokenRequest.builder()
				.grantType(REFRESH_TOKEN)
				.refreshToken(REFRESH_TOKEN_ID.toString())
				.build();
		when(clientRefreshTokenRepository.findById(REFRESH_TOKEN_ID))
				.thenReturn(Optional.of(ClientRefreshToken.builder()
						.clientId(CLIENT_ID)
						.scope(AuthorizationScope.READ_PHONE_NUMBER.getCode())
						.username(USERNAME)
						.build()));
		when(accessTokenCreator.createToken(USERNAME, List.of(AuthorizationScope.READ_PHONE_NUMBER)))
				.thenReturn(ACCESS_TOKEN);
		var tokenResponseResult = refreshTokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID.toString());
		assertThat(tokenResponseResult.getResult()).isEqualTo(TokenResponse.builder().accessToken(ACCESS_TOKEN).build());
	}

}
