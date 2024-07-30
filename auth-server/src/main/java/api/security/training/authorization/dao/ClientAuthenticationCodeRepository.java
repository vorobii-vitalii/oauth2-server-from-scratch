package api.security.training.authorization.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import api.security.training.authorization.domain.ClientAuthenticationCode;

@Repository
public interface ClientAuthenticationCodeRepository extends ListCrudRepository<ClientAuthenticationCode, UUID> {
	Optional<ClientAuthenticationCode> findByAuthorizationRequestId(UUID authorizationRequestId);
}
