package api.security.training.authorization.handler;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.dao.ClientAuthenticationCodeRepository;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.domain.ClientAuthenticationCode;
import api.security.training.authorization.utils.URIParametersAppender;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CodeAuthorizationRedirectStrategy implements AuthorizationRedirectStrategy {
	private static final String CODE_RESPONSE_TYPE = "code";

	private final ClientAuthenticationCodeRepository clientAuthenticationCodeRepository;
	private final Supplier<UUID> uuidSupplier;
	private final URIParametersAppender uriParametersAppender;

	@SneakyThrows
	@Override
	public String computeAuthorizationRedirectURL(AuthorizationRequest authorizationRequest) {
		var clientAuthenticationCode = ClientAuthenticationCode.builder()
				.code(uuidSupplier.get())
				.clientId(authorizationRequest.clientId())
				.authorizationRequestId(authorizationRequest.id())
				.scope(authorizationRequest.scope())
				.state(authorizationRequest.state())
				.username(authorizationRequest.username())
				.build();
		clientAuthenticationCodeRepository.save(clientAuthenticationCode);
		var parameters = new HashMap<String, String>();
		parameters.put("code", clientAuthenticationCode.code().toString());
		if (authorizationRequest.state() != null) {
			parameters.put("state", authorizationRequest.state());
		}
		return uriParametersAppender.appendParameters(authorizationRequest.redirectURL(), parameters);
	}

	@Override
	public boolean canHandleResponseType(String responseType) {
		return CODE_RESPONSE_TYPE.equals(responseType);
	}
}
