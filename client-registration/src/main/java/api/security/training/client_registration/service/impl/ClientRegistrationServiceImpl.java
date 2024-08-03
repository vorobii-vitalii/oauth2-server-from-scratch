package api.security.training.client_registration.service.impl;

import java.util.UUID;
import java.util.function.Supplier;

import com.spencerwi.either.Result;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.api.dto.RegisterClientResponse;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.client_registration.domain.ClientRegistration;
import api.security.training.client_registration.secret.ClientSecretSupplier;
import api.security.training.client_registration.service.ClientRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationServiceImpl implements ClientRegistrationService {
	private final ClientRegistrationRepository clientRegistrationRepository;
	private final Supplier<UUID> uuidSupplier;
	private final ClientSecretSupplier clientSecretSupplier;

	@Override
	public Result<RegisterClientResponse> registerClient(RegisterClientRequest registerClientRequest) {
		var clientName = registerClientRequest.name();
		var alreadyExists = clientRegistrationRepository.existsByClientName(clientName);
		if (alreadyExists) {
			log.warn("Client by name {} already exists", clientName);
			return Result.err(new IllegalArgumentException("Client with such name already exists"));
		} else {
			var newClientId = uuidSupplier.get();
			var clientSecret = clientSecretSupplier.createClientSecret();
			var clientRegistration = ClientRegistration.builder()
					.clientId(newClientId)
					// Not encrypt for now for simplicity
					.clientSecretEncrypted(clientSecret.clientSecretEncrypted())
					.clientName(clientName)
					.clientDescription(registerClientRequest.description())
					.clientType(registerClientRequest.clientType().getValue())
					.redirectURL(registerClientRequest.redirectUrl().toString())
					.build();
			clientRegistrationRepository.save(clientRegistration);
			log.info("Successfully inserted new client {}", clientRegistration);
			return Result.ok(new RegisterClientResponse(newClientId, clientSecret.clientSecretPlainText()));
		}
	}
}
