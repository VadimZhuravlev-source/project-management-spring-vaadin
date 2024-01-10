package com.pmvaadin.resources.frontend.elements;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.resources.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.frontend.views.LaborResourceForm;

public class LaborResourceList extends ItemList<LaborResourceRepresentation, LaborResource> {

    private final ListService<LaborResourceRepresentation, LaborResource> listService;
    private LaborResourceForm editingForm;

    public LaborResourceList(ListService<LaborResourceRepresentation, LaborResource> listService) {
        super(listService);
        this.listService = listService;
        configureGrid();
    }

    private void configureGrid() {

        this.grid.addColumn(LaborResourceRepresentation::getName).setHeader("Name");
        onMouseDoubleClick(this::openNewItem);

        beforeAddition(this::openNewItem);
        onCoping(this::openNewItem);

    }

    private void openNewItem(LaborResource laborResource) {
        openEditingForm(laborResource);
    }

    private void openEditingForm(LaborResource laborResource) {
        editingForm = new LaborResourceForm();
        editingForm.read(laborResource);
        editingForm.addListener(LaborResourceForm.SaveEvent.class, this::saveEvent);
        editingForm.addListener(LaborResourceForm.CloseEvent.class, closeEvent -> closeEditor());
        editingForm.open();
        setDeletionAvailable(false);
    }

    private void saveEvent(LaborResourceForm.SaveEvent event) {
        editingForm.close();
        var item = event.getItem();
        if (item instanceof LaborResource laborResource) {
            listService.save(laborResource);
            this.grid.getDataProvider().refreshAll();
        }
    }

    private void closeEditor() {
        editingForm.close();
        setDeletionAvailable(true);
        this.grid.getDataProvider().refreshAll();
    }

}
