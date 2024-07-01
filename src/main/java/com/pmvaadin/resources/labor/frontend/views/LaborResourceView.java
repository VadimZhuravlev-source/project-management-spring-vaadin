package com.pmvaadin.resources.labor.frontend.views;

import com.pmvaadin.MainLayout;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.resources.labor.frontend.elements.LaborResourceList;
import com.pmvaadin.resources.labor.services.LaborResourceService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Qualifier;

@Route(value = "LaborResources", layout = MainLayout.class)
@PageTitle("Labor resources | PM")
@RolesAllowed({"ADMIN", "PROJECT_MANAGER"})
public class LaborResourceView extends VerticalLayout {

    private final LaborResourceService laborResourceService;

    public LaborResourceView(@Qualifier("LaborResourceService") LaborResourceService laborResourceService) {

        this.laborResourceService = laborResourceService;
        if (!(laborResourceService instanceof ListService)) {
            return;
        }

        var list = new LaborResourceList((ListService) laborResourceService);
        add(list);

    }

}
