package com.pmvaadin;

import com.pmvaadin.terms.calendars.view.CalendarsView;
import com.pmvaadin.projectview.ProjectTreeView;
import com.pmvaadin.security.services.SecurityService;
import com.pmvaadin.security.AdminUsersView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    private final SecurityService securityService;

    public MainLayout() {
        this.securityService = new SecurityService();
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H6 logo = new H6("Project management");
        logo.addClassNames("text-l", "m-m");

        Button logout = new Button("Log out",  click ->
                securityService.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo
                , logout
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);

    }

    private void createDrawer() {
        RouterLink projectTasksLink = new RouterLink("Projects", ProjectTreeView.class);
        projectTasksLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink calendarsLink = new RouterLink("Calendars", CalendarsView.class);
        calendarsLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink usersLink = new RouterLink("Users", AdminUsersView.class);
        calendarsLink.setHighlightCondition(HighlightConditions.sameLocation());

//        RouterLink tests = new RouterLink("Tests", TestElementsView.class);
//        tests.setHighlightCondition(HighlightConditions.sameLocation());

        addToDrawer(new VerticalLayout(
                projectTasksLink,
                calendarsLink,
                usersLink
                //,
                //tests
        ));
    }

}
