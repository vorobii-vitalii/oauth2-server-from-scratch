package api.security.training.authorization.impl;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.spencerwi.either.Either;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.ObtainResourceOwnerConsentService;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.dto.ResourceOwnerAuthorizationRequest;
import api.security.training.authorization.dto.ResourceOwnerConsentRequest;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObtainResourceOwnerConsentServiceImpl implements ObtainResourceOwnerConsentService {
	private final AuthorizationRequestRepository authorizationRequestRepository;
	private final ClientRegistrationRepository clientRegistrationRepository;
	private final Supplier<UUID> uuidSupplier;
	private final List<AuthorizationRedirectStrategy> authorizationRedirectStrategies;

	@Override
	public Either<ResourceOwnerConsentRequest, String> obtainResourceOwnerConsent(ResourceOwnerAuthorizationRequest request) {
		return null;
	}
}
