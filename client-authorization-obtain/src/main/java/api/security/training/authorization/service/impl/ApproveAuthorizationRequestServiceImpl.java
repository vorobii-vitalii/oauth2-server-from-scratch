package api.security.training.authorization.service.impl;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import com.spencerwi.either.Result;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.dto.ApproveAuthorizationRequest;
import api.security.training.authorization.service.ApproveAuthorizationRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ApproveAuthorizationRequestServiceImpl implements ApproveAuthorizationRequestService {
	private final AuthorizationRequestRepository authorizationRequestRepository;
	private final List<AuthorizationRedirectStrategy> authorizationRedirectStrategies;

	@Override
	public Result<URI> approveAuthorizationRequest(ApproveAuthorizationRequest request) {
		var authRequestId = request.authorizationRequestId();
		log.info("Checking whether authentication request by id = {} exists", authRequestId);
		var authRequestOpt = authorizationRequestRepository.findById(authRequestId);
		if (authRequestOpt.isEmpty()) {
			log.warn("Authentication request is absent in DB!");
			return Result.err(new IllegalArgumentException("Authentication request was not found!"));
		} else {
			var authorizationRequest = authRequestOpt.get();
			if (Objects.equals(authorizationRequest.username(), request.validator())) {
				log.info("Performing redirect...");
				var authorizationRedirectHandler = authorizationRedirectStrategies.stream()
						.filter(v -> v.canHandleResponseType(authorizationRequest.responseType()))
						.findFirst()
						.orElseThrow();
				return Result.attempt(() -> authorizationRedirectHandler.computeAuthorizationRedirectURL(authorizationRequest));
			} else {
				return Result.err(new IllegalArgumentException("You tried to approve request not requested by you!"));
			}
		}
	}
}
