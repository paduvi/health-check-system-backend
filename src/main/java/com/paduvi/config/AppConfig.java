package com.paduvi.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import graphql.servlet.GraphQLErrorHandler;

@Configuration
public class AppConfig implements WebMvcConfigurer {

	@Autowired
	private Environment environment;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("dev")))) {
			registry.addMapping("/**").allowedMethods("*");
		}
	}

	@Bean
	public GraphQLErrorHandler errorHandler() {
		return new GraphQLErrorHandler() {
			@Override
			public List<GraphQLError> processErrors(List<GraphQLError> errors) {
				List<GraphQLError> clientErrors = errors.stream().filter(this::isClientError)
						.collect(Collectors.toList());
				List<GraphQLError> serverErrors = errors.stream().filter(e -> !isClientError(e))
						.map(GraphQLErrorAdapter::new).collect(Collectors.toList());
				List<GraphQLError> e = new ArrayList<>();
				e.addAll(clientErrors);
				e.addAll(serverErrors);
				return e;
			}

			protected boolean isClientError(GraphQLError error) {
				return !(error instanceof ExceptionWhileDataFetching || error instanceof Throwable);
			}
		};
	}

}

class GraphQLErrorAdapter implements GraphQLError {
	private GraphQLError error;

	public GraphQLErrorAdapter(GraphQLError error) {
		this.error = error;
	}

	@Override
	public Map<String, Object> getExtensions() {
		return error.getExtensions();
	}

	@Override
	public List<SourceLocation> getLocations() {
		return error.getLocations();
	}

	@Override
	public ErrorType getErrorType() {
		return error.getErrorType();
	}

	@Override
	public List<Object> getPath() {
		return error.getPath();
	}

	@Override
	public Map<String, Object> toSpecification() {
		return error.toSpecification();
	}

	@Override
	public String getMessage() {
		return (error instanceof ExceptionWhileDataFetching)
				? ((ExceptionWhileDataFetching) error).getException().getMessage() : error.getMessage();
	}
}
