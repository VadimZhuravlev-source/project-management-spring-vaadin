package com.pmvaadin.costs.labor.frontend.elements;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentation;
import com.pmvaadin.costs.labor.frontend.views.LaborCostForm;
import com.pmvaadin.resources.labor.frontend.views.LaborResourceForm;

public class LaborCostList extends ItemList<LaborCostRepresentation, LaborCost> {

    private final ListService<LaborCostRepresentation, LaborCost> listService;
    private LaborCostForm editingForm;

    public LaborCostList(ListService<LaborCostRepresentation, LaborCost> listService) {
        super(listService);
        this.listService = listService;
        configureGrid();
    }

    private void configureGrid() {

        this.grid.addColumn(LaborCostRepresentation::getName).setHeader("Name");
        onMouseDoubleClick(this::openNewItem);

        beforeAddition(this::openNewItem);
        onCoping(this::openNewItem);
        onContextMenuOpen(this::openNewItem);
    }

    private void openNewItem(LaborCost laborCost) {
        openEditingForm(laborCost);
    }

    private void openEditingForm(LaborCost laborCost) {
        editingForm = new LaborCostForm();
        editingForm.read(laborCost);
        editingForm.addListener(LaborCostForm.SaveEvent.class, this::saveEvent);
        editingForm.addListener(LaborCostForm.CloseEvent.class, closeEvent -> closeEditor());
        editingForm.open();
        setDeletionAvailable(false);
    }

    private void saveEvent(LaborCostForm.SaveEvent event) {
        var item = event.getItem();
        if (item instanceof LaborCost laborCost) {
            if (editingForm.isOpened()) {
                var savedResource = listService.save(laborCost);
                editingForm.read(savedResource);
            } else
                this.grid.getDataProvider().refreshAll();
        }
    }

    private void closeEditor() {
        editingForm.close();
        setDeletionAvailable(true);
        this.grid.getDataProvider().refreshAll();
    }

}
