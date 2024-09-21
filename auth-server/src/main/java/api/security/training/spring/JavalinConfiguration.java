package api.security.training.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import api.security.training.request.RequestParameterService;
import api.security.training.request.impl.RequestParameterServiceImpl;

@Configuration
public class JavalinConfiguration {

	@Bean
	RequestParameterService requestParameterService() {
		return new RequestParameterServiceImpl();
	}

}
