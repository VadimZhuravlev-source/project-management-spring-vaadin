package com.pmvaadin.terms.timeunit.frontend.views;

import com.pmvaadin.MainLayout;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.terms.timeunit.frontend.elements.TimeUnitList;
import com.pmvaadin.terms.timeunit.services.TimeUnitService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "TimeUnits", layout = MainLayout.class)
@PageTitle("Time units | PM")
@RolesAllowed({"ADMIN", "PROJECT_MANAGER"})
public class TimeUnitsView extends VerticalLayout {

    public TimeUnitsView(TimeUnitService timeUnitService) {

        if (!(timeUnitService instanceof ListService)) {
            return;
        }

        var list = new TimeUnitList((ListService) timeUnitService);
        add(list);

    }

}
