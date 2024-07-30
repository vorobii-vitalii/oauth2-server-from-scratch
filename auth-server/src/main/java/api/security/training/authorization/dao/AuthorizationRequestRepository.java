package api.security.training.authorization.dao;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import api.security.training.authorization.domain.AuthorizationRequest;

@Repository
public interface AuthorizationRequestRepository extends CrudRepository<AuthorizationRequest, UUID> {
}
