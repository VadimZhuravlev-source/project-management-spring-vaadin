package com.pmvaadin.terms.calendars.frontend.view;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.terms.calendars.frontend.elements.CalendarList;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.pmvaadin.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "Calendars", layout = MainLayout.class)
@PageTitle("Calendars | PM")
@RolesAllowed({"ADMIN", "PROJECT_MANAGER"})
public class CalendarsView extends VerticalLayout {

    public CalendarsView(CalendarService calendarService, CalendarForm calendarForm) {

        if (!(calendarService instanceof ListService)) {
            return;
        }

        var element = new CalendarList((ListService) calendarService, calendarForm);
        add(element);

    }

}