package api.security.training.authorization;

import com.spencerwi.either.Either;

import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.dto.ResourceOwnerConsentRequest;

public interface ObtainResourceOwnerConsentService {
	Either<ResourceOwnerConsentRequest, String> obtainResourceOwnerConsent(ResourceOwnerAuthorizationRequest request);
}
