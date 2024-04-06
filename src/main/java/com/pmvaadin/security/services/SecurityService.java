package com.pmvaadin.security.services;

import com.pmvaadin.security.entities.Role;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";
    @Autowired
    private UserService userService;

    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) context.getAuthentication().getPrincipal();
        }
        // Anonymous or no authentication.
        return null;
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);
    }

//    public Role getUserRole() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null) {
//            return null;
//        }
//        String currentPrincipalName = authentication.getName();
//        var user = userService.getUserByName(currentPrincipalName);
//        if (user == null) {
//            return null;
//        }
//        if (user.getRoles().contains(Role.ADMIN))
//            return Role.ADMIN;
//        else if (user.getRoles().contains(Role.PROJECT_MANAGER)) {
//            return Role.PROJECT_MANAGER;
//        } else if (user.getRoles().contains(Role.WORKER)) {
//            return Role.WORKER;
//        }
//        return null;
//    }

}
