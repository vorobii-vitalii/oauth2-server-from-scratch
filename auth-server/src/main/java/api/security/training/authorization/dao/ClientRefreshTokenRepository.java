package api.security.training.authorization.dao;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import api.security.training.authorization.domain.ClientRefreshToken;

@Repository
public interface ClientRefreshTokenRepository extends ListCrudRepository<ClientRefreshToken, UUID> {
}
