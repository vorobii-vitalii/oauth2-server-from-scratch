package api.security.training.client_registration.service;

import com.spencerwi.either.Either;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.api.dto.RegisterClientResponse;
import api.security.training.client_registration.dto.ClientAlreadyExistsError;

public interface ClientRegistrationService {
	Either<RegisterClientResponse, ClientAlreadyExistsError> registerClient(RegisterClientRequest registerClientRequest);
}
