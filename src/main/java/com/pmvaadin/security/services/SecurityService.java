package com.pmvaadin.security.services;

import com.pmvaadin.security.LoginUserDetails;
import com.pmvaadin.security.entities.Role;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";

    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);
    }

    public Role getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        var principal = authentication.getPrincipal();
        UserDetails userDetails;
        if (principal instanceof UserDetails) {
            userDetails = (UserDetails) principal;
        } else
            return null;

        var rolePrefix = LoginUserDetails.ROLE_PREFIX;
        for (GrantedAuthority grantedAuthority: userDetails.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals(rolePrefix + Role.ADMIN.name()))
                return Role.ADMIN;
        }
        for (GrantedAuthority grantedAuthority: userDetails.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals(rolePrefix + Role.PROJECT_MANAGER.name()))
                return Role.PROJECT_MANAGER;
        }
        for (GrantedAuthority grantedAuthority: userDetails.getAuthorities()) {
            if (grantedAuthority.getAuthority().equals(rolePrefix + Role.WORKER.name()))
                return Role.WORKER;
        }

        return null;
    }

}
