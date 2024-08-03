package api.security.training.authorization.service;

import java.net.URI;

import com.spencerwi.either.Result;

import api.security.training.authorization.dto.RejectAuthorizationRequest;

public interface RejectAuthorizationRequestService {
	Result<URI> rejectAuthorizationRequest(RejectAuthorizationRequest rejectAuthorizationRequest);
}
