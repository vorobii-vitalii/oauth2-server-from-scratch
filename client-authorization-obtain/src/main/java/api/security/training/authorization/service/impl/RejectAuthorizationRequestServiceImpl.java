package api.security.training.authorization.service.impl;

import java.util.HashMap;
import java.util.Objects;

import com.spencerwi.either.Result;

import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.dto.RejectAuthorizationRequest;
import api.security.training.authorization.service.RejectAuthorizationRequestService;
import api.security.training.authorization.utils.URIParametersAppender;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RejectAuthorizationRequestServiceImpl implements RejectAuthorizationRequestService {
	private final AuthorizationRequestRepository authorizationRequestRepository;
	private final URIParametersAppender uriParametersAppender;

	@SneakyThrows
	@Override
	public Result<String> rejectAuthorizationRequest(RejectAuthorizationRequest rejectAuthorizationRequest) {
		var authRequestId = rejectAuthorizationRequest.authorizationRequestId();
		log.info("Checking whether authentication request by id = {} exists", authRequestId);
		var authRequestOpt = authorizationRequestRepository.findById(authRequestId);
		if (authRequestOpt.isEmpty()) {
			log.warn("Authentication request is absent in DB!");
			return Result.err(new IllegalArgumentException("Authentication request was not found"));
		} else {
			var authorizationRequest = authRequestOpt.get();
			if (Objects.equals(authorizationRequest.username(), rejectAuthorizationRequest.validator())) {
				log.info("Performing rejected redirect...");
				var parameters = new HashMap<String, String>();
				parameters.put("error", "access_denied");
				if (authorizationRequest.state() != null) {
					parameters.put("state", authorizationRequest.state());
				}
				return Result.ok(uriParametersAppender.appendParameters(authorizationRequest.redirectURL(), parameters));
			} else {
				return Result.err(new IllegalStateException("You tried to reject request not requested by you!"));
			}
		}
	}
}
