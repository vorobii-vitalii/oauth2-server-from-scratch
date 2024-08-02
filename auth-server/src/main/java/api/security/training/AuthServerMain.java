package api.security.training;

import java.nio.file.Path;
import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.authorization.handler.ApproveAuthorizationRequestHandler;
import api.security.training.authorization.handler.AuthorizationCodeTokenRequestHandler;
import api.security.training.authorization.handler.AuthorizationHandler;
import api.security.training.authorization.handler.CodeAuthorizationRedirectStrategy;
import api.security.training.authorization.handler.ImplicitAuthorizationRedirectStrategy;
import api.security.training.authorization.handler.RejectAuthorizationRequestHandler;
import api.security.training.authorization.handler.TokenHandler;
import api.security.training.client_registration.dao.ClientRegistrationRepository;
import api.security.training.client_registration.secret.impl.ClientSecretSupplierImpl;
import api.security.training.client_registration.service.impl.ClientRegistrationServiceImpl;
import api.security.training.exception.AuthenticationRequiredException;
import api.security.training.filters.UserAuthenticationFilter;
import api.security.training.handlers.ClientRegistrationHandler;
import api.security.training.handlers.LoginHandler;
import api.security.training.handlers.UserRegistrationHandler;
import api.security.training.spring.RootConfig;
import api.security.training.token.impl.JwtAccessTokenCreator;
import api.security.training.token.impl.JwtAccessTokenInfoReader;
import api.security.training.users.dao.UserRepository;
import api.security.training.users.login.service.impl.UserCredentialsCheckerImpl;
import api.security.training.users.login.service.impl.UserLoginServiceImpl;
import api.security.training.users.password.impl.NaivePasswordService;
import api.security.training.users.registration.service.impl.UserRegistrationServiceImpl;
import api.security.training.validation.ValidatingBodyHandler;
import api.security.training.validation.impl.SimpleErrorsListValidationErrorResponseFactory;
import api.security.training.authorization.AuthorizationRedirectStrategy;
import api.security.training.authorization.dao.AuthorizationRequestRepository;
import api.security.training.authorization.dao.ClientAuthenticationCodeRepository;
import api.security.training.authorization.dao.ClientRefreshTokenRepository;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Validation;
import lombok.extern.slf4j.Slf4j;
import pi.security.training.ResourceOwnerCredentialsTokenRequestHandler;

@Slf4j
public class AuthServerMain {
	public static final String SECRET = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";
	public static final int TOKEN_EXPIRATION_IN_MS = 300_000;

	public static void main(String[] args) {
		var app = Javalin.create(config -> {
			var codeResolver = new DirectoryCodeResolver(
					Path.of(Objects.requireNonNull(AuthServerMain.class.getClassLoader().getResource("templates")).getFile()));
			config.fileRenderer(new JavalinJte(TemplateEngine.create(codeResolver, ContentType.Html)));
			config.requestLogger.http((ctx, ms) -> log.info("Request {} IP = {} Headers = {}", ctx.url(), ctx.ip(), ctx.headerMap()));
		});
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RootConfig.class);

		var authorizationRequestRepository = applicationContext.getBean(AuthorizationRequestRepository.class);
		var clientRegistrationRepository = applicationContext.getBean(ClientRegistrationRepository.class);
		var userRepository = applicationContext.getBean(UserRepository.class);
		var clientAuthenticationCodeRepository = applicationContext.getBean(ClientAuthenticationCodeRepository.class);
		var clientRefreshTokenRepository = applicationContext.getBean(ClientRefreshTokenRepository.class);

		var validator = Validation.buildDefaultValidatorFactory().getValidator();

		var signKey = createSignKey();
		var tokenCreator = new JwtAccessTokenCreator(signKey, Date::new, TOKEN_EXPIRATION_IN_MS);
		var tokenInfoReader = new JwtAccessTokenInfoReader(signKey, Date::new);
		var requestTokenExtractor = new CookieRequestTokenExtractor();

		app.before("/authorize", new UserAuthenticationFilter(tokenInfoReader, requestTokenExtractor));

		List<AuthorizationRedirectStrategy> authorizationRedirectStrategies = List.of(
				new ImplicitAuthorizationRedirectStrategy(tokenCreator),
				new CodeAuthorizationRedirectStrategy(clientAuthenticationCodeRepository, UUID::randomUUID)
		);
		var passwordService = new NaivePasswordService();

		app.get("/authorize", new AuthorizationHandler(requestTokenExtractor, tokenInfoReader, authorizationRequestRepository, clientRegistrationRepository, UUID::randomUUID, authorizationRedirectStrategies));
		var userCredentialsChecker = new UserCredentialsCheckerImpl(userRepository, passwordService);
		app.post("/token", new TokenHandler(
				List.of(
						new AuthorizationCodeTokenRequestHandler(
								clientAuthenticationCodeRepository,
								UUID::randomUUID,
								clientRefreshTokenRepository,
								Clock.systemUTC(),
								tokenCreator
						),
						new ResourceOwnerCredentialsTokenRequestHandler(
								userCredentialsChecker,
								tokenCreator,
								UUID::randomUUID,
								clientRefreshTokenRepository,
								Clock.systemUTC()
						)
				),
				clientRegistrationRepository
		));

		app.post("/approve/{authRequestId}", new ApproveAuthorizationRequestHandler(authorizationRequestRepository, tokenInfoReader, requestTokenExtractor, authorizationRedirectStrategies));
		app.post("/reject/{authRequestId}", new RejectAuthorizationRequestHandler(authorizationRequestRepository, tokenInfoReader, requestTokenExtractor));
		app.post("/register", new UserRegistrationHandler(new UserRegistrationServiceImpl(passwordService, userRepository, UUID::randomUUID)));
		app.post("/login", new LoginHandler(new UserLoginServiceImpl(userCredentialsChecker, tokenCreator), TOKEN_EXPIRATION_IN_MS));

		app.post("/clients", new ValidatingBodyHandler<>(
				validator,
				new SimpleErrorsListValidationErrorResponseFactory(),
				new ClientRegistrationHandler(new ClientRegistrationServiceImpl(
						clientRegistrationRepository,
						UUID::randomUUID,
						new ClientSecretSupplierImpl()
				)),
				RegisterClientRequest.class
		));

		app.exception(AuthenticationRequiredException.class, (exception, ctx) -> ctx.render("login.jte", Map.of(
				"loginPageParams", LoginPageParams.builder()
						.redirectTo(exception.getRedirectTo())
						.build()
		)));

		app.start(7000);

		Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
		app.events(event -> {
			event.serverStopping(() -> log.info("Server is stopping"));
			event.serverStopped(() -> log.info("Server stopped"));
		});
	}

	private static Key createSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}

}
