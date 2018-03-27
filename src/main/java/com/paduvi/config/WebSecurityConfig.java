package com.paduvi.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import com.paduvi.controller.ExceptionHandlingController;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthFailureHandler authFailureHandler;

	@Autowired
	private AuthSuccessHandler authSuccessHandler;

	@Autowired
	private AjaxAwareLoginUrlAuthenticationEntryPoint ajaxAwareLoginUrlAuthenticationEntryPoint;

	@Autowired
	private Environment environment;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("dev")))) {
			http.authorizeRequests().antMatchers("/api/**").permitAll();
			http.csrf().disable();
		}

		http.authorizeRequests().antMatchers("/resources/**", "/favicon.ico", ExceptionHandlingController.ERROR_PATH)
				.permitAll().anyRequest().authenticated();
		http.exceptionHandling().authenticationEntryPoint(ajaxAwareLoginUrlAuthenticationEntryPoint);
		http.formLogin().loginPage("/login").failureHandler(authFailureHandler).successHandler(authSuccessHandler)
				.permitAll();
		http.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).permitAll();

	}

	@Bean
	@Override
	public UserDetailsService userDetailsService() {
		UserDetails user = User.withUsername("recommender").password("{noop}recommender").roles("ADMIN").build();

		return new InMemoryUserDetailsManager(user);
	}

}

@Component
class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().print("{\"success\": false}");
		response.getWriter().flush();
	}
}

@Component
class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print("{\"success\": true}");
		response.getWriter().flush();
	}
}

@Component
class AjaxAwareLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	public AjaxAwareLoginUrlAuthenticationEntryPoint(@Value("/login") String loginFormUrl) {
		super(loginFormUrl);
	}

	@Override
	public void commence(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException authException) throws IOException, ServletException {
		if (isPreflight(request)) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else if (isRestRequest(request)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().print("{\"success\": false}");
			response.getWriter().flush();
		} else {
			super.commence(request, response, authException);
		}
	}

	/**
	 * Checks if this is a X-domain pre-flight request.
	 * 
	 * @param request
	 * @return
	 */
	private boolean isPreflight(HttpServletRequest request) {
		return "OPTIONS".equals(request.getMethod());
	}

	/**
	 * Checks if it is a rest request
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isRestRequest(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			if (key.equalsIgnoreCase("X-CSRF-TOKEN"))
				return true;
		}
		return false;
	}
}
