package api.security.training.client_registration.service;

import com.spencerwi.either.Result;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.api.dto.RegisterClientResponse;

public interface ClientRegistrationService {
	Result<RegisterClientResponse> registerClient(RegisterClientRequest registerClientRequest);
}
