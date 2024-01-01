package com.pmvaadin.resources.views;

import com.pmvaadin.MainLayout;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.resources.services.LaborResourceService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "laborResources", layout = MainLayout.class)
@PageTitle("Time units | PM")
@PermitAll
public class LaborResourceView extends VerticalLayout {

    private LaborResourceForm editingForm;
    private final LaborResourceService laborResourceService;

    public LaborResourceView(LaborResourceService laborResourceService) {

        this.laborResourceService = laborResourceService;
        if (!(laborResourceService instanceof ListService)) {
            return;
        }

        var list = new Table((ListService) laborResourceService);
        add(list);

    }

    private class Table extends ItemList<LaborResource, LaborResource> {

        Table(ListService<LaborResource, LaborResource> listService) {
            super(listService);
            configureGrid();
        }

        private void configureGrid() {

            this.grid.addColumn(LaborResource::getName).setHeader("Name");
            onMouseDoubleClick(this::openNewItem);

            beforeAddition(this::openNewItem);
            onCoping(this::openNewItem);

        }

        private void openNewItem(LaborResource laborResource) {
            openEditingForm(laborResource);
        }

        private void openEditingForm(LaborResource laborResource) {
            editingForm = new LaborResourceForm(laborResource);
            editingForm.addListener(LaborResourceForm.SaveEvent.class, this::saveEvent);
            editingForm.addListener(LaborResourceForm.CloseEvent.class, closeEvent -> closeEditor());
            editingForm.open();
            setDeletionAvailable(false);
        }

        private void saveEvent(LaborResourceForm.SaveEvent event) {
            editingForm.close();
            var laborResource = event.getLaborResource();
            if (laborResource != null)
                laborResourceService.save(laborResource);
            this.grid.getDataProvider().refreshAll();
        }

        private void closeEditor() {
            editingForm.close();
            setDeletionAvailable(true);
            this.grid.getDataProvider().refreshAll();
        }

    }

}
