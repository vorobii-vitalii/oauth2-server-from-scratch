package api.security.training.authorization.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.dto.RejectAuthorizationRequest;
import api.security.training.authorization.utils.URIParametersAppender;

@ExtendWith(MockitoExtension.class)
class TestRejectAuthorizationRequestServiceImpl {

	@Mock
	AuthorizationRequestRepository authorizationRequestRepository;

	@Mock
	URIParametersAppender uriParametersAppender;

	@InjectMocks
	RejectAuthorizationRequestServiceImpl rejectAuthorizationRequestService;

	@Test
	void givenAuthorizationRequestNotFound() {
		var authorizationRequestId = UUID.randomUUID();
		var validator = "myUsername";
		when(authorizationRequestRepository.findById(authorizationRequestId)).thenReturn(Optional.empty());
		var result = rejectAuthorizationRequestService.rejectAuthorizationRequest(RejectAuthorizationRequest.builder()
				.authorizationRequestId(authorizationRequestId)
				.validator(validator)
				.build());
		assertThat(result.isErr()).isTrue();
	}

	@Test
	void givenValidatorMismatchForAuthorizationRequest() {
		var authorizationRequestId = UUID.randomUUID();
		var validator = "myUsername";
		when(authorizationRequestRepository.findById(authorizationRequestId)).thenReturn(Optional.of(
				AuthorizationRequest.builder()
						.id(authorizationRequestId)
						.username("different validator")
						.build()
		));
		var result = rejectAuthorizationRequestService.rejectAuthorizationRequest(RejectAuthorizationRequest.builder()
				.authorizationRequestId(authorizationRequestId)
				.validator(validator)
				.build());
		assertThat(result.isErr()).isTrue();
	}

	@Test
	void givenStateParameterWasNotPassedInitiallyByClient() {
		var authorizationRequestId = UUID.randomUUID();
		var validator = "myUsername";
		var redirectURL = URI.create("http://localhost/callback");
		when(authorizationRequestRepository.findById(authorizationRequestId)).thenReturn(Optional.of(
				AuthorizationRequest.builder()
						.id(authorizationRequestId)
						.username(validator)
						.redirectURL(redirectURL)
						.build()
		));
		when(uriParametersAppender.appendParameters(redirectURL, Map.of("error", "access_denied")))
				.thenReturn(URI.create("http://localhost/callback?error=access_denied"));
		var result = rejectAuthorizationRequestService.rejectAuthorizationRequest(RejectAuthorizationRequest.builder()
				.authorizationRequestId(authorizationRequestId)
				.validator(validator)
				.build());
		assertThat(result.getResult()).isEqualTo(URI.create("http://localhost/callback?error=access_denied"));
	}

	@Test
	void givenStateParameterWasPassedInitiallyByClient() {
		var authorizationRequestId = UUID.randomUUID();
		var validator = "myUsername";
		var redirectURL = URI.create("http://localhost/callback");
		when(authorizationRequestRepository.findById(authorizationRequestId)).thenReturn(Optional.of(
				AuthorizationRequest.builder()
						.id(authorizationRequestId)
						.username(validator)
						.redirectURL(redirectURL)
						.state("xyz")
						.build()
		));
		when(uriParametersAppender.appendParameters(redirectURL, Map.of("error", "access_denied", "state", "xyz")))
				.thenReturn(URI.create("http://localhost/callback?error=access_denied&state=xyz"));
		var result = rejectAuthorizationRequestService.rejectAuthorizationRequest(RejectAuthorizationRequest.builder()
				.authorizationRequestId(authorizationRequestId)
				.validator(validator)
				.build());
		assertThat(result.getResult()).isEqualTo(URI.create("http://localhost/callback?error=access_denied&state=xyz"));
	}

}
