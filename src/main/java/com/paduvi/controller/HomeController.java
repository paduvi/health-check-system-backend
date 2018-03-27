package com.paduvi.controller;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@RequestMapping("/login")
	String login() throws Exception {
		boolean isLoggedIn = false;
		if (SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() != null) {

			for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
				if (authority.getAuthority().equals("ROLE_ADMIN")) {
					isLoggedIn = true;
				}
			}
		}

		String viewName = isLoggedIn ? "redirect:/" : "login";
		return viewName;
	}
	
}
