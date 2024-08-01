package api.security.training.client_registration.dao;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import api.security.training.client_registration.domain.ClientRegistration;

public interface ClientRegistrationRepository extends ListCrudRepository<ClientRegistration, UUID> {
	boolean existsByClientName(String clientName);
}
