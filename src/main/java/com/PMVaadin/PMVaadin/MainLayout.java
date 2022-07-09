package com.PMVaadin.PMVaadin;

import com.PMVaadin.PMVaadin.CalendarsView.CalendarsView;
import com.PMVaadin.PMVaadin.ProjectView.ProjectTasksView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {

        createHeader();
        createDrawer();

    }

    private void createHeader() {
        H1 logo = new H1("Project management");
        logo.addClassNames("text-l", "m-m");

        Button logout = new Button("Log out");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);

    }

    private void createDrawer() {
        RouterLink projectTasksLink = new RouterLink("Project", ProjectTasksView.class);
        projectTasksLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink calendarsLink = new RouterLink("Calendars", CalendarsView.class);
        calendarsLink.setHighlightCondition(HighlightConditions.sameLocation());

        addToDrawer(new VerticalLayout(
                projectTasksLink,
                calendarsLink
        ));
    }

}
