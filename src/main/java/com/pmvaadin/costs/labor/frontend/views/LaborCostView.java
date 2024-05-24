package com.pmvaadin.costs.labor.frontend.views;

import com.pmvaadin.MainLayout;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.costs.labor.services.LaborCostService;
import com.pmvaadin.resources.labor.frontend.elements.LaborResourceList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "actualLaborCosts", layout = MainLayout.class)
@PageTitle("Actual labor costs | PM")
@PermitAll
public class LaborCostView extends VerticalLayout {

    private final LaborCostService laborCostService;

    public LaborCostView(LaborCostService laborCostService) {

        this.laborCostService = laborCostService;
        if (!(laborCostService instanceof ListService)) {
            return;
        }

        var list = new LaborResourceList((ListService) laborCostService);
        add(list);

    }

}
