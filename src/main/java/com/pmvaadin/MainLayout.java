package com.pmvaadin;

import com.pmvaadin.costs.labor.frontend.views.LaborCostView;
import com.pmvaadin.resources.labor.frontend.views.LaborResourceView;
import com.pmvaadin.security.entities.Role;
import com.pmvaadin.terms.calendars.frontend.view.CalendarsView;
import com.pmvaadin.project.view.ProjectTreeView;
import com.pmvaadin.security.services.SecurityService;
import com.pmvaadin.security.frontend.views.AdminUsersView;
import com.pmvaadin.terms.timeunit.frontend.views.TimeUnitsView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({"ADMIN, PROJECT_MANAGER, WORKER"})
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

        // TODO add user name in the header, how to do that is on the vaadin site in the section concerning security
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

        var userRole = securityService.getUserRole();
        if (userRole == null)
            return;

        var verticalLayout = new VerticalLayout();
        // TODO put partition by roles in a method
        if (userRole == Role.ADMIN || userRole == Role.PROJECT_MANAGER) {
            RouterLink projectTasksLink = new RouterLink("Projects", ProjectTreeView.class);
            projectTasksLink.setHighlightCondition(HighlightConditions.sameLocation());

            RouterLink calendarsLink = new RouterLink("Calendars", CalendarsView.class);
            calendarsLink.setHighlightCondition(HighlightConditions.sameLocation());

            RouterLink timeUnitLink = new RouterLink("Time units", TimeUnitsView.class);
            timeUnitLink.setHighlightCondition(HighlightConditions.sameLocation());

            RouterLink laborResourceLink = new RouterLink("Labor resources", LaborResourceView.class);
            laborResourceLink.setHighlightCondition(HighlightConditions.sameLocation());

            verticalLayout.add(
                    projectTasksLink,
                    calendarsLink,
                    timeUnitLink,
                    laborResourceLink);

            if (userRole == Role.ADMIN) {
                RouterLink usersLink = new RouterLink("Users", AdminUsersView.class);
                usersLink.setHighlightCondition(HighlightConditions.sameLocation());
                verticalLayout.add(usersLink);
            }
        }

        RouterLink actualLaborCosts = new RouterLink("Actual labor costs", LaborCostView.class);
        actualLaborCosts.setHighlightCondition(HighlightConditions.sameLocation());
        verticalLayout.add(actualLaborCosts);

//        RouterLink tests = new RouterLink("Tests", TestElementsView.class);
//        tests.setHighlightCondition(HighlightConditions.sameLocation());

        addToDrawer(verticalLayout);
    }


}
