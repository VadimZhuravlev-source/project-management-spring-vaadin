package com.pmvaadin.security.frontend.views;

import com.pmvaadin.MainLayout;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.security.frontend.elements.UserList;
import com.pmvaadin.security.services.UserService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | PM")
@RolesAllowed({"ADMIN"})
public class AdminUsersView extends VerticalLayout {

    public AdminUsersView(UserService userService, UserForm userForm) {

        if (!(userService instanceof ListService)) {
            return;
        }
        var userList = new UserList((ListService) userService, userForm);

        add(userList);
    }

}
