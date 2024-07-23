package api.security.training;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import api.security.training.api.dto.RegisterClientRequest;
import api.security.training.api.handler.ClientRegistrationHandler;
import api.security.training.registration.impl.ClientSecretSupplierImpl;
import api.security.training.validation.ValidatingBodyHandler;
import api.security.training.validation.impl.SimpleErrorsListValidationErrorResponseFactory;
import io.javalin.Javalin;
import io.javalin.vue.VueComponent;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class AuthServerMain {

	public static void main(String[] args) {
		var app = Javalin.create(config -> {
			config.staticFiles.enableWebjars();
			config.vue.vueInstanceNameInJs = "app";
			config.vue.rootDirectory("/vue");
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

		app.get("/", new VueComponent("hello-world"));
		app.post("/clients", new ValidatingBodyHandler<>(
				validator,
				new SimpleErrorsListValidationErrorResponseFactory(),
				new ClientRegistrationHandler(entityTemplate, UUID::randomUUID, new ClientSecretSupplierImpl()),
				RegisterClientRequest.class
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

	private static <T> Mono<Optional<T>> toOptionalMono(Mono<T> mono) {
		return mono.map(Optional::ofNullable).switchIfEmpty(Mono.just(Optional.empty()));
	}

}
