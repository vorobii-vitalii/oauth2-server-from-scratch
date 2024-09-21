package api.security.training.users.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import api.security.training.users.domain.User;

public interface UserRepository extends ListCrudRepository<User, UUID> {
	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);
}
