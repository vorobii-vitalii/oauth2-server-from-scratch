package api.security.training;

import java.io.File;
import java.nio.file.Path;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.client_registration.handler.ClientRegistrationHandler;
import api.security.training.client_registration.ClientSecretSupplierImpl;
import api.security.training.exception.AuthenticationRequiredException;
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
import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.HttpStatus;
import io.javalin.rendering.template.JavalinJte;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthServerMain {
	public static final String SECRET = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";
	public static final int TOKEN_EXPIRATION_IN_MS = 300_000;

	public static void main(String[] args) {
		var app = Javalin.create(config -> {
//			config.staticFiles.enableWebjars();
			final JavalinJte fileRenderer = new JavalinJte(
					TemplateEngine.create(new DirectoryCodeResolver(Path.of(Objects.requireNonNull(AuthServerMain.class.getClassLoader().getResource("templates")).getFile())), ContentType.Html)
			);
			config.fileRenderer(fileRenderer);
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

		R2dbcEntityTemplate entityTemplate = new R2dbcEntityTemplate(connectionFactory);

		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		var signKey = createSignKey();
		var tokenCreator = new JwtTokenCreator(signKey, Date::new, TOKEN_EXPIRATION_IN_MS);
		var tokenInfoReader = new TokenInfoReaderImpl(signKey, Date::new);

		app.before("/home", new UserAuthenticationFilter(tokenInfoReader, ctx -> new AuthenticationRequiredException("/home")));
		app.exception(AuthenticationRequiredException.class, (exception, ctx) -> {
//			ctx.redirect("/login/?redirectOnSuccess=" + exception.getRedirectTo());
			ctx.render("login.jte", Map.of(
					"loginPageParams", LoginPageParams.builder()
							.redirectTo(exception.getRedirectTo())
							.build()
			));
		});

		app.get("/home", ctx -> {
			ctx.render("main-page.jte");
			ctx.status(HttpStatus.OK);
		});
		app.post("/register", new UserRegistrationHandler(new NaivePasswordService(), entityTemplate, UUID::randomUUID));
		app.get("/login", ctx -> {
			String redirectOnSuccess = ctx.queryParam("redirectOnSuccess");

			ctx.render("login.jte", Map.of(
					"loginPageParams", LoginPageParams.builder()
							.redirectTo(redirectOnSuccess)
							.build()
			));
		});
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
