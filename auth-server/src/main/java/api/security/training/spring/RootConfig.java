package api.security.training.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DBConfiguration.class)
@ComponentScan("api.security.training")
public class RootConfig  {
}
