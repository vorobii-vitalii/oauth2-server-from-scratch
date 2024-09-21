package api.security.training.authorization.service;

import java.net.URI;

import com.spencerwi.either.Result;

import api.security.training.authorization.dto.ApproveAuthorizationRequest;

public interface ApproveAuthorizationRequestService {
	Result<URI> approveAuthorizationRequest(ApproveAuthorizationRequest request);
}
