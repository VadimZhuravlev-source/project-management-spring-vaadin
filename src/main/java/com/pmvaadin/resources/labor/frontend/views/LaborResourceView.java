package com.pmvaadin.resources.labor.frontend.views;

import com.pmvaadin.MainLayout;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.resources.labor.frontend.elements.LaborResourceList;
import com.pmvaadin.resources.labor.services.LaborResourceService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "laborResources", layout = MainLayout.class)
@PageTitle("Time units | PM")
@PermitAll
public class LaborResourceView extends VerticalLayout {

    private final LaborResourceService laborResourceService;

    public LaborResourceView(LaborResourceService laborResourceService) {

        this.laborResourceService = laborResourceService;
        if (!(laborResourceService instanceof ListService)) {
            return;
        }

        var list = new LaborResourceList((ListService) laborResourceService);
        add(list);

    }

}
