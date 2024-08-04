package api.security.training.handlers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import api.security.training.token.AccessTokenInfoReader;
import api.security.training.token.dto.AuthorizationScope;
import api.security.training.users.dao.UserRepository;
import api.security.training.users.domain.User;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserInfoProvider implements Handler {
	private final Map<String, AuthorizationScope> requiredScope = Map.of(
			"first_name", AuthorizationScope.READ_FIRST_NAME,
			"last_name", AuthorizationScope.READ_LAST_NAME,
			"phone_number", AuthorizationScope.READ_PHONE_NUMBER
	);
	private final Map<String, Function<User, String>> extractorMap = Map.of(
			"first_name", User::firstName,
			"last_name", User::lastName,
			"phone_number", User::phoneNumber
	);
	private static final String BEARER_SUFFIX = "Bearer ";

	private final UserRepository userRepository;
	private final AccessTokenInfoReader accessTokenInfoReader;

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		var authHeader = ctx.header(Header.AUTHORIZATION);
		if (authHeader == null) {
			ctx.status(HttpStatus.UNAUTHORIZED);
			return;
		}
		if (!authHeader.startsWith(BEARER_SUFFIX)) {
			log.warn("Auth header value ({}) doesn't start with {}", authHeader, BEARER_SUFFIX);
			ctx.status(HttpStatus.UNAUTHORIZED);
			return;
		}
		var accessToken = authHeader.substring(BEARER_SUFFIX.length());
		var tokenInfo = accessTokenInfoReader.readTokenInfo(accessToken);
		if (tokenInfo.isExpired()) {
			log.warn("Token expired");
			ctx.status(HttpStatus.UNAUTHORIZED);
			return;
		}
		var param = ctx.pathParam("param");
		var requiredScope = this.requiredScope.get(param);
		if (!tokenInfo.authScopes().contains(requiredScope)) {
			log.warn("No scope = {}", requiredScope);
			ctx.status(HttpStatus.UNAUTHORIZED);
			ctx.json(List.of("You don't have permission to " + requiredScope.getDisplayName()));
			return;
		}
		var user = userRepository.findByUsername(tokenInfo.username()).orElseThrow();
		ctx.status(HttpStatus.OK);
		ctx.json(extractorMap.get(param).apply(user));
	}
}
