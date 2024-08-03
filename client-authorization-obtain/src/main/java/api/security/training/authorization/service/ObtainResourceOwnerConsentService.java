package api.security.training.authorization.service;

import java.net.URI;

import com.spencerwi.either.Either;
import com.spencerwi.either.Result;

import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.dto.ResourceOwnerConsentRequest;

public interface ObtainResourceOwnerConsentService {
	Result<Either<ResourceOwnerConsentRequest, URI>> obtainResourceOwnerConsent(ResourceOwnerAuthorizationRequest request);
}
