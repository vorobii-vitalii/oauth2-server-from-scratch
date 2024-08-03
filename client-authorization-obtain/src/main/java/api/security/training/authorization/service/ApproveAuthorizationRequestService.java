package api.security.training.authorization.service;

import com.spencerwi.either.Result;

import api.security.training.authorization.dto.ApproveAuthorizationRequest;

public interface ApproveAuthorizationRequestService {
	Result<String> approveAuthorizationRequest(ApproveAuthorizationRequest request);
}
