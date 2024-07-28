package api.security.training;

import java.nio.file.Path;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.authorization.handler.ApproveAuthorizationRequestHandler;
import api.security.training.authorization.handler.AuthorizationHandler;
import api.security.training.authorization.handler.ImplicitAuthorizationRedirectHandler;
import api.security.training.client_registration.ClientSecretSupplierImpl;
import api.security.training.client_registration.handler.ClientRegistrationHandler;
import api.security.training.exception.AuthenticationRequiredException;
import api.security.training.token.impl.CookieRequestTokenExtractor;
import api.security.training.token.impl.JwtTokenCreator;
import api.security.training.token.impl.TokenInfoReaderImpl;
import api.security.training.users.auth.UserAuthenticationFilter;
import api.security.training.users.login.dto.LoginPageParams;
import api.security.training.users.login.handler.LoginHandler;
import api.security.training.users.password.impl.NaivePasswordService;
import api.security.training.users.registration.dto.UserRegistrationRequest;
import api.security.training.users.registration.handler.UserRegistrationHandler;
import api.security.training.validation.ValidatingBodyHandler;
import api.security.training.validation.impl.SimpleErrorsListValidationErrorResponseFactory;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import jakarta.validation.Validation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthServerMain {
	public static final String SECRET = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";
	public static final int TOKEN_EXPIRATION_IN_MS = 300_000;

	public static void main(String[] args) {
		var app = Javalin.create(config -> {
			var codeResolver = new DirectoryCodeResolver(
					Path.of(Objects.requireNonNull(AuthServerMain.class.getClassLoader().getResource("templates")).getFile()));
			config.fileRenderer(new JavalinJte(TemplateEngine.create(codeResolver, ContentType.Html)));
			config.requestLogger.http((ctx, ms) -> {
				log.info("Request {} IP = {} Headers = {}", ctx.url(), ctx.ip(), ctx.headerMap());
			});
		});
		ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
				.option(ConnectionFactoryOptions.DRIVER, "postgresql")
				.option(ConnectionFactoryOptions.HOST, "localhost")
				.option(ConnectionFactoryOptions.PORT, 5432)
				.option(ConnectionFactoryOptions.USER, "myuser")
				.option(ConnectionFactoryOptions.PASSWORD, "secret")
				.option(ConnectionFactoryOptions.DATABASE, "mydatabase")
				.build());

		var entityTemplate = new R2dbcEntityTemplate(connectionFactory);
		var validator = Validation.buildDefaultValidatorFactory().getValidator();

		var signKey = createSignKey();
		var tokenCreator = new JwtTokenCreator(signKey, Date::new, TOKEN_EXPIRATION_IN_MS);
		var tokenInfoReader = new TokenInfoReaderImpl(signKey, Date::new);
		var requestTokenExtractor = new CookieRequestTokenExtractor();

		app.before("/authorize",
				new UserAuthenticationFilter(tokenInfoReader, requestTokenExtractor, ctx -> {
					String queryParams = ctx.queryParamMap()
							.entrySet()
							.stream()
							.flatMap(v -> v.getValue().stream().map(q -> v.getKey() + "=" + q))
							.collect(Collectors.joining("&"));
					// "/authorize/?" + queryParams
					return new AuthenticationRequiredException("/authorize/?" + queryParams);
				}));

		app.get("/authorize", new AuthorizationHandler(requestTokenExtractor, tokenInfoReader, entityTemplate, UUID::randomUUID));
		app.post("/approve/{authRequestId}", new ApproveAuthorizationRequestHandler(entityTemplate, tokenInfoReader, requestTokenExtractor, List.of(
				new ImplicitAuthorizationRedirectHandler(tokenCreator)
		)));
		app.post("/reject/{authRequestId}", ctx -> {

		});

		app.post("/register", new UserRegistrationHandler(new NaivePasswordService(), entityTemplate, UUID::randomUUID));
		app.post("/login", new LoginHandler(entityTemplate, new NaivePasswordService(), tokenCreator, TOKEN_EXPIRATION_IN_MS));

		app.post("/clients", new ValidatingBodyHandler<>(
				validator,
				new SimpleErrorsListValidationErrorResponseFactory(),
				new ClientRegistrationHandler(entityTemplate, UUID::randomUUID, new ClientSecretSupplierImpl()),
				RegisterClientRequest.class
		));
		app.post("/users", new ValidatingBodyHandler<>(
				validator,
				new SimpleErrorsListValidationErrorResponseFactory(),
				new UserRegistrationHandler(new NaivePasswordService(), entityTemplate, UUID::randomUUID),
				UserRegistrationRequest.class
		));

		app.exception(AuthenticationRequiredException.class, (exception, ctx) -> ctx.render("login.jte", Map.of(
				"loginPageParams", LoginPageParams.builder()
						.redirectTo(exception.getRedirectTo())
						.build()
		)));

		app.start(7000);

		Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
		app.events(event -> {
			event.serverStopping(() -> {
				log.info("Server is stopping");
			});
			event.serverStopped(() -> {
				log.info("Server stopped");
			});
		});
	}

	private static Key createSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}

}
