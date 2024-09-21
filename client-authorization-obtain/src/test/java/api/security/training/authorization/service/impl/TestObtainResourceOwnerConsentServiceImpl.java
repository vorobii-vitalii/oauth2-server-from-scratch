package api.security.training.authorization.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.utils.URIParametersAppender;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.client_registration.domain.ClientRegistration;

@ExtendWith(MockitoExtension.class)
class TestObtainResourceOwnerConsentServiceImpl {
	private static final UUID RANDOM_UUID = UUID.randomUUID();
	private static final UUID CLIENT_ID = UUID.randomUUID();
	private static final String RESPONSE_TYPE = "responseType";
	private static final URI RESULTING_URI = URI.create("http://host/callback?param1=x");

	@Mock
	AuthorizationRequestRepository authorizationRequestRepository;

	@Mock
	ClientRegistrationRepository clientRegistrationRepository;

	@Mock
	AuthorizationRedirectStrategy authorizationRedirectStrategy;

	@Mock
	URIParametersAppender uriParametersAppender;

	ObtainResourceOwnerConsentServiceImpl obtainResourceOwnerConsentService;

	@BeforeEach
	void init() {
		obtainResourceOwnerConsentService = new ObtainResourceOwnerConsentServiceImpl(
				authorizationRequestRepository,
				clientRegistrationRepository,
				() -> RANDOM_UUID,
				List.of(authorizationRedirectStrategy),
				uriParametersAppender
		);
	}

	@Test
	void givenClientIdNull() {
		ResourceOwnerAuthorizationRequest request = ResourceOwnerAuthorizationRequest.builder()
				.build();
		assertThat(obtainResourceOwnerConsentService.obtainResourceOwnerConsent(request).isErr()).isTrue();
	}

	@Test
	void givenClientNotFound() {
		ResourceOwnerAuthorizationRequest request = ResourceOwnerAuthorizationRequest.builder()
				.clientId(CLIENT_ID.toString())
				.build();
		when(clientRegistrationRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());
		assertThat(obtainResourceOwnerConsentService.obtainResourceOwnerConsent(request).isErr()).isTrue();
	}

	@Test
	void givenRedirectURIMismatch() {
		ResourceOwnerAuthorizationRequest request = ResourceOwnerAuthorizationRequest.builder()
				.clientId(CLIENT_ID.toString())
				.redirectURI(URI.create("http://other_host/callback"))
				.build();
		when(clientRegistrationRepository.findById(CLIENT_ID)).thenReturn(Optional.of(
				ClientRegistration.builder()
						.clientId(CLIENT_ID)
						.redirectURL(URI.create("http://host/callback"))
						.build()
		));
		assertThat(obtainResourceOwnerConsentService.obtainResourceOwnerConsent(request).isErr()).isTrue();
	}

	@Test
	void givenResponseTypeNotSpecified() {
		ResourceOwnerAuthorizationRequest request = ResourceOwnerAuthorizationRequest.builder()
				.clientId(CLIENT_ID.toString())
				.build();
		when(clientRegistrationRepository.findById(CLIENT_ID)).thenReturn(Optional.of(
				ClientRegistration.builder()
						.clientId(CLIENT_ID)
						.redirectURL(URI.create("http://host/callback"))
						.build()
		));
		when(uriParametersAppender.appendParameters(URI.create("http://host/callback"),
				Map.of("error", "invalid_request", "error_description", "Response type not specified")))
				.thenReturn(RESULTING_URI);
		assertThat(obtainResourceOwnerConsentService.obtainResourceOwnerConsent(request).getResult().getRight())
				.isEqualTo(RESULTING_URI);
	}

	@Test
	void givenResponseTypeNotSupported() {
		ResourceOwnerAuthorizationRequest request = ResourceOwnerAuthorizationRequest.builder()
				.clientId(CLIENT_ID.toString())
				.responseType(RESPONSE_TYPE)
				.build();
		when(clientRegistrationRepository.findById(CLIENT_ID)).thenReturn(Optional.of(
				ClientRegistration.builder()
						.clientId(CLIENT_ID)
						.redirectURL(URI.create("http://host/callback"))
						.build()
		));
		when(authorizationRedirectStrategy.canHandleResponseType(RESPONSE_TYPE)).thenReturn(false);
		when(uriParametersAppender.appendParameters(URI.create("http://host/callback"),
				Map.of("error", "unsupported_response_type", "error_description", "Response type not supported")))
				.thenReturn(RESULTING_URI);
		assertThat(obtainResourceOwnerConsentService.obtainResourceOwnerConsent(request).getResult().getRight())
				.isEqualTo(RESULTING_URI);
	}

	@Test
	void givenInvalidScopes() {
		ResourceOwnerAuthorizationRequest request = ResourceOwnerAuthorizationRequest.builder()
				.clientId(CLIENT_ID.toString())
				.responseType(RESPONSE_TYPE)
				.scope("invalid")
				.build();
		when(clientRegistrationRepository.findById(CLIENT_ID)).thenReturn(Optional.of(
				ClientRegistration.builder()
						.clientId(CLIENT_ID)
						.redirectURL(URI.create("http://host/callback"))
						.build()
		));
		when(authorizationRedirectStrategy.canHandleResponseType(RESPONSE_TYPE)).thenReturn(true);
		when(uriParametersAppender.appendParameters(URI.create("http://host/callback"),
				Map.of("error", "invalid_scope", "error_description", "Invalid scopes")))
				.thenReturn(RESULTING_URI);
		assertThat(obtainResourceOwnerConsentService.obtainResourceOwnerConsent(request).getResult().getRight())
				.isEqualTo(RESULTING_URI);
	}


}
