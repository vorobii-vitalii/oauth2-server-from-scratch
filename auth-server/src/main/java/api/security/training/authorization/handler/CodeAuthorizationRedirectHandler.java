package api.security.training.authorization.handler;

import org.apache.hc.core5.net.URIBuilder;

import api.security.training.authorization.AuthorizationRedirectHandler;
import api.security.training.authorization.dao.ClientAuthenticationCodeRepository;
import api.security.training.authorization.domain.AuthorizationRequest;
import api.security.training.authorization.domain.ClientAuthenticationCode;
import api.security.training.UUIDSupplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CodeAuthorizationRedirectHandler implements AuthorizationRedirectHandler {
	private static final String CODE_RESPONSE_TYPE = "code";

	private final ClientAuthenticationCodeRepository clientAuthenticationCodeRepository;
	private final UUIDSupplier uuidSupplier;

	@SneakyThrows
	@Override
	public String handleAuthorizationRedirect(AuthorizationRequest authorizationRequest) {
		var clientAuthenticationCode = ClientAuthenticationCode.builder()
				.code(uuidSupplier.createUUID())
				.clientId(authorizationRequest.clientId())
				.authorizationRequestId(authorizationRequest.id())
				.scope(authorizationRequest.scope())
				.state(authorizationRequest.state())
				.username(authorizationRequest.username())
				.build();
		clientAuthenticationCodeRepository.save(clientAuthenticationCode);
		var uriBuilder = new URIBuilder(authorizationRequest.redirectURL())
				.addParameter("code", clientAuthenticationCode.code().toString());
		if (authorizationRequest.state() != null) {
			uriBuilder.addParameter("state", authorizationRequest.state());
		}
		return uriBuilder.build().toString();
	}

	@Override
	public boolean canHandleResponseType(String responseType) {
		return CODE_RESPONSE_TYPE.equals(responseType);
	}
}
