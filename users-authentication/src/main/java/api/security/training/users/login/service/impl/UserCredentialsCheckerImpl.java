package api.security.training.users.login.service.impl;

import api.security.training.users.dao.UserRepository;
import api.security.training.users.login.service.UserCredentialsChecker;
import api.security.training.users.password.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserCredentialsCheckerImpl implements UserCredentialsChecker {
	private final UserRepository userRepository;
	private final PasswordService passwordService;

	@Override
	public boolean areCredentialsCorrect(String username, String password) {
		var foundUser = userRepository.findByUsername(username);
		if (foundUser.isPresent()) {
			log.info("User found. Verifying password...");
			var actualPasswordHash = foundUser.get().password();
			if (passwordService.isPasswordCorrect(actualPasswordHash, password)) {
				log.info("Password is correct!");
				return true;
			} else {
				log.warn("Password is wrong...");
				return false;
			}
		} else {
			log.warn("User with such username {} not found...", username);
			return false;
		}
	}
}
