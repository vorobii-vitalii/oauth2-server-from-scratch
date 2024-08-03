package api.security.training.authorization.service;

import com.spencerwi.either.Either;

import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.dto.ResourceOwnerConsentRequest;

public interface ObtainResourceOwnerConsentService {
	Either<ResourceOwnerConsentRequest, Either<String, String>> obtainResourceOwnerConsent(ResourceOwnerAuthorizationRequest request);
}
