package org.ttarena.arena_user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("The TT Arena User API").description("API for managing users. ").version("v0.1")
						.contact(new Contact().name("David Cuadrado").email("dcuadradomart@gmail.com")))
				.components(new Components().addSecuritySchemes("bearer-key",
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer ").bearerFormat("JWT")))
				.addTagsItem(new Tag().name("User Management").description("Operations related to user management"));
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder().group("public-api").pathsToMatch("/user/**", "/home/**").build();
	}

	@Bean
	public GroupedOpenApi userApi() {
		return GroupedOpenApi.builder().group("user-api").pathsToMatch("/user/**").build();
	}

}
