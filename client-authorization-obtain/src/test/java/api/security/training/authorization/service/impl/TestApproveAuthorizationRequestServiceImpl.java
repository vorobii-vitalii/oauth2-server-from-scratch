package api.security.training.authorization.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.dto.ApproveAuthorizationRequest;
import api.security.training.authorization.service.ApproveAuthorizationRequestService;

@ExtendWith(MockitoExtension.class)
class TestApproveAuthorizationRequestServiceImpl {

	@Mock
	AuthorizationRequestRepository authorizationRequestRepository;

	@Mock
	AuthorizationRedirectStrategy authorizationRedirectStrategy;

	ApproveAuthorizationRequestService approveAuthorizationRequestService;

	@BeforeEach
	void init() {
		approveAuthorizationRequestService = new ApproveAuthorizationRequestServiceImpl(
				authorizationRequestRepository, List.of(authorizationRedirectStrategy));
	}

	@Test
	void givenAuthorizationRequestNotFound() {
		var authorizationRequestId = UUID.randomUUID();
		var validator = "username";
		when(authorizationRequestRepository.findById(authorizationRequestId)).thenReturn(Optional.empty());
		var result = approveAuthorizationRequestService.approveAuthorizationRequest(ApproveAuthorizationRequest.builder()
				.authorizationRequestId(authorizationRequestId)
				.validator(validator)
				.build());
		assertThat(result.isErr()).isTrue();
	}

	@Test
	void givenValidatorUsernameMismatch() {
		var authorizationRequestId = UUID.randomUUID();
		var validator = "username";
		when(authorizationRequestRepository.findById(authorizationRequestId)).thenReturn(Optional.of(
				AuthorizationRequest.builder()
						.id(authorizationRequestId)
						.username("different validator")
						.responseType("token")
						.build()
		));
		var result = approveAuthorizationRequestService.approveAuthorizationRequest(ApproveAuthorizationRequest.builder()
				.authorizationRequestId(authorizationRequestId)
				.validator(validator)
				.build());
		assertThat(result.isErr()).isTrue();
	}

	@Test
	void givenValidatorUsernameMatch() {
		var authorizationRequestId = UUID.randomUUID();
		var validator = "username";
		final AuthorizationRequest authorizationRequest = AuthorizationRequest.builder()
				.id(authorizationRequestId)
				.username(validator)
				.responseType("token")
				.build();
		when(authorizationRequestRepository.findById(authorizationRequestId)).thenReturn(Optional.of(authorizationRequest));
		when(authorizationRedirectStrategy.canHandleResponseType("token")).thenReturn(true);
		var redirectURI = URI.create("http://localhost:80/redirect?token=123");
		when(authorizationRedirectStrategy.computeAuthorizationRedirectURL(authorizationRequest))
				.thenReturn(redirectURI);
		var result = approveAuthorizationRequestService.approveAuthorizationRequest(ApproveAuthorizationRequest.builder()
				.authorizationRequestId(authorizationRequestId)
				.validator(validator)
				.build());
		assertThat(result.getResult()).isEqualTo(redirectURI);
	}

}
