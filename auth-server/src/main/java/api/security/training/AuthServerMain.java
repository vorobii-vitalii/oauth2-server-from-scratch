package api.security.training;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.authorization.service.ApproveAuthorizationRequestService;
import api.security.training.authorization.service.ObtainResourceOwnerConsentService;
import api.security.training.authorization.service.RejectAuthorizationRequestService;
import api.security.training.authorization.service.TokenRequestService;
import api.security.training.client_registration.service.ClientRegistrationService;
import api.security.training.dto.LoginPageParams;
import api.security.training.exception.AuthenticationRequiredException;
import api.security.training.filters.UserAuthenticationFilter;
import api.security.training.filters.UserInfoFilter;
import api.security.training.handlers.ApproveAuthorizationRequestHandler;
import api.security.training.handlers.AuthorizationHandler;
import api.security.training.handlers.ClientRegistrationHandler;
import api.security.training.handlers.LoginHandler;
import api.security.training.handlers.RejectAuthorizationRequestHandler;
import api.security.training.handlers.TokenHandler;
import api.security.training.handlers.UserRegistrationHandler;
import api.security.training.handlers.ValidatingBodyHandler;
import api.security.training.request.RequestParameterService;
import api.security.training.request.impl.CookieRequestTokenExtractor;
import api.security.training.spring.RootConfig;
import api.security.training.token.AccessTokenCreator;
import api.security.training.token.AccessTokenInfoReader;
import api.security.training.users.login.service.UserCredentialsChecker;
import api.security.training.users.registration.service.UserRegistrationService;
import api.security.training.validation.impl.SimpleErrorsListValidationErrorResponseFactory;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.rendering.template.JavalinJte;
import jakarta.validation.Validation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthServerMain {
	public static final int TOKEN_EXPIRATION_IN_MS = 300_000;

	public static void main(String[] args) {
		var app = Javalin.create(config -> {
			var codeResolver = new DirectoryCodeResolver(
					Path.of(Objects.requireNonNull(AuthServerMain.class.getClassLoader().getResource("templates")).getFile()));
			config.fileRenderer(new JavalinJte(TemplateEngine.create(codeResolver, ContentType.Html)));
			config.requestLogger.http((ctx, ms) -> log.info("Request {} IP = {} Headers = {}", ctx.url(), ctx.ip(), ctx.headerMap()));
		});
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RootConfig.class);

		var validator = Validation.buildDefaultValidatorFactory().getValidator();

		var tokenCreator = applicationContext.getBean(AccessTokenCreator.class);
		var tokenInfoReader = applicationContext.getBean(AccessTokenInfoReader.class);
		var requestTokenExtractor = new CookieRequestTokenExtractor();
		var requestParameterService = applicationContext.getBean(RequestParameterService.class);

		app.before(new UserInfoFilter(tokenInfoReader, requestTokenExtractor, requestParameterService));

		app.before("/authorize", new UserAuthenticationFilter(requestParameterService));

		var obtainResourceOwnerConsentService = applicationContext.getBean(ObtainResourceOwnerConsentService.class);
		app.get("/authorize", new AuthorizationHandler(requestParameterService, obtainResourceOwnerConsentService));
		var userCredentialsChecker = applicationContext.getBean(UserCredentialsChecker.class);
		app.post("/token", new TokenHandler(applicationContext.getBean(TokenRequestService.class)));

		app.post("/approve/{authRequestId}", new ApproveAuthorizationRequestHandler(
				requestParameterService, applicationContext.getBean(ApproveAuthorizationRequestService.class)));
		app.post("/reject/{authRequestId}", new RejectAuthorizationRequestHandler(
				applicationContext.getBean(RejectAuthorizationRequestService.class), requestParameterService));
		app.post("/register", new UserRegistrationHandler(applicationContext.getBean(UserRegistrationService.class)));
		app.post("/login", new LoginHandler(userCredentialsChecker, tokenCreator, TOKEN_EXPIRATION_IN_MS));

		app.post("/clients", new ValidatingBodyHandler<>(
				validator,
				new SimpleErrorsListValidationErrorResponseFactory(),
				new ClientRegistrationHandler(applicationContext.getBean(ClientRegistrationService.class)),
				RegisterClientRequest.class
		));

		app.exception(AuthenticationRequiredException.class, (exception, ctx) -> ctx.render("login.jte", Map.of(
				"loginPageParams", LoginPageParams.builder().redirectTo(exception.getRedirectTo()).build()
		)));
		app.exception(IllegalArgumentException.class, (err, ctx) -> {
			ctx.status(HttpStatus.BAD_REQUEST);
			ctx.json(Map.of("error", err.getMessage()));
		});

		app.start(7000);

		Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
		app.events(event -> {
			event.serverStopping(() -> log.info("Server is stopping"));
			event.serverStopped(() -> log.info("Server stopped"));
		});
	}

}
