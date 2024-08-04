package api.security.training.authorization.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spencerwi.either.Result;

import api.security.training.api.dto.TokenRequest;
import api.security.training.api.dto.TokenResponse;
import api.security.training.authorization.TokenRequestHandler;
import api.security.training.authorization.dto.ClientCredentials;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.client_registration.domain.ClientRegistration;

@ExtendWith(MockitoExtension.class)
class TestTokenRequestServiceImpl {
	private static final String GRANT_TYPE = "grantType";
	private static final String CLIENT_ID = UUID.randomUUID().toString();
	private static final String CLIENT_SECRET = "clientSecret";
	private static final ClientCredentials CLIENT_CREDENTIALS = ClientCredentials.builder().clientId(CLIENT_ID).clientSecret(CLIENT_SECRET).build();

	@Mock
	ClientRegistrationRepository clientRegistrationRepository;

	@Mock
	TokenRequestHandler tokenRequestHandler;

	TokenRequestServiceImpl tokenRequestService;

	@BeforeEach
	void init() {
		tokenRequestService = new TokenRequestServiceImpl(clientRegistrationRepository, List.of(tokenRequestHandler));
	}

	@Test
	void givenNoneStrategySupportTheGrantType() {
		var tokenRequest = TokenRequest.builder().grantType(GRANT_TYPE).build();
		when(tokenRequestHandler.canHandleGrantType(GRANT_TYPE)).thenReturn(false);
		var tokenResponseResult = tokenRequestService.handleTokenRequest(tokenRequest, CLIENT_CREDENTIALS);
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void givenClientRegistrationNotFound() {
		var tokenRequest = TokenRequest.builder().grantType(GRANT_TYPE).build();
		when(tokenRequestHandler.canHandleGrantType(GRANT_TYPE)).thenReturn(true);
		when(clientRegistrationRepository.findById(UUID.fromString(CLIENT_ID))).thenReturn(Optional.empty());
		var tokenResponseResult = tokenRequestService.handleTokenRequest(tokenRequest, CLIENT_CREDENTIALS);
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void givenInvalidClientSecret() {
		var tokenRequest = TokenRequest.builder().grantType(GRANT_TYPE).build();
		when(tokenRequestHandler.canHandleGrantType(GRANT_TYPE)).thenReturn(true);
		when(clientRegistrationRepository.findById(UUID.fromString(CLIENT_ID)))
				.thenReturn(Optional.of(ClientRegistration.builder().clientSecretEncrypted("123").build()));
		var tokenResponseResult = tokenRequestService.handleTokenRequest(tokenRequest, CLIENT_CREDENTIALS);
		assertThat(tokenResponseResult.isErr()).isTrue();
	}

	@Test
	void happyPath() {
		var tokenRequest = TokenRequest.builder().grantType(GRANT_TYPE).build();
		when(tokenRequestHandler.canHandleGrantType(GRANT_TYPE)).thenReturn(true);
		when(clientRegistrationRepository.findById(UUID.fromString(CLIENT_ID)))
				.thenReturn(Optional.of(ClientRegistration.builder().clientSecretEncrypted(CLIENT_SECRET).build()));
		var strategyTokenResponseResult = Result.ok(TokenResponse.builder().accessToken("123").build());
		when(tokenRequestHandler.handleTokenRequest(tokenRequest, CLIENT_ID))
				.thenReturn(strategyTokenResponseResult);
		var tokenResponseResult = tokenRequestService.handleTokenRequest(tokenRequest, CLIENT_CREDENTIALS);
		assertThat(tokenResponseResult.getResult()).isEqualTo(TokenResponse.builder().accessToken("123").build());
	}

}