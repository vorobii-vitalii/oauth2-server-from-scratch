package api.security.training.authorization.handler;

import org.jetbrains.annotations.NotNull;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RejectAuthorizationRequestHandler implements Handler {

	@Override
	public void handle(@NotNull Context ctx) throws Exception {

	}

}
