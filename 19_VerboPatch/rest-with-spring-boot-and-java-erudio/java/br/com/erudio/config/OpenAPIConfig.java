package br.com.erudio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenAPIConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
				.title("My first RESTful API with Java 17 and Spring Boot")
				.version("v1")
				.description("")
				.termsOfService("http://url")
				.license(new License()
						.name("Apache 2.0").url("http://url")));
	}
	
}
