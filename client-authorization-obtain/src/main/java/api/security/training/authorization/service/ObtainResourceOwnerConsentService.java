package api.security.training.authorization.service;

import com.spencerwi.either.Either;
import com.spencerwi.either.Result;

import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.dto.ResourceOwnerConsentRequest;

public interface ObtainResourceOwnerConsentService {
	// TODO: Use URI type
	Result<Either<ResourceOwnerConsentRequest, String>> obtainResourceOwnerConsent(ResourceOwnerAuthorizationRequest request);
}
