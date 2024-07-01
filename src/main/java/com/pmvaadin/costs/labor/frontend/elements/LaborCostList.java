package com.pmvaadin.costs.labor.frontend.elements;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentation;
import com.pmvaadin.costs.labor.frontend.views.LaborCostForm;
import com.pmvaadin.costs.labor.services.LaborCostService;
import com.pmvaadin.resources.labor.frontend.elements.FilteredLaborResourceComboBox;

public class LaborCostList extends ItemList<LaborCostRepresentation, LaborCost> {

    private final LaborCostService laborCostService;
    private final ListService<LaborCostRepresentation, LaborCost> listService;
    private final FilteredLaborResourceComboBox resourceComboBox;
    private LaborCostForm editingForm;

    public LaborCostList(LaborCostService listService, FilteredLaborResourceComboBox resourceComboBox) {
        super((ListService<LaborCostRepresentation, LaborCost>) listService);
        this.laborCostService = listService;
        this.listService = (ListService<LaborCostRepresentation, LaborCost>) listService;
        this.resourceComboBox = resourceComboBox;
        configureGrid();
    }

    private void configureGrid() {

        this.grid.addColumn(LaborCostRepresentation::getRepresentation).setHeader("Name");
        this.grid.addColumn(LaborCostRepresentation::getDay).setHeader("Day");
        this.grid.addColumn(LaborCostRepresentation::getResourceName).setHeader("Resource");
        onMouseDoubleClick(this::openNewItem);

        beforeAddition(this::openNewItem);
        onCoping(this::openNewItem);
        onContextMenuOpen(this::openNewItem);
    }

    private void openNewItem(LaborCost laborCost) {
        openEditingForm(laborCost);
    }

    private void openEditingForm(LaborCost laborCost) {
        editingForm = new LaborCostForm(laborCostService, resourceComboBox);
        editingForm.read(laborCost);
        editingForm.addListener(LaborCostForm.SaveEvent.class, this::saveEvent);
        editingForm.addListener(LaborCostForm.SaveAndCloseEvent.class, this::saveAndCloseEvent);
        editingForm.addListener(LaborCostForm.CloseEvent.class, event -> closeEditor());
        editingForm.addListener(LaborCostForm.RefreshEvent.class, this::refreshEvent);
        editingForm.open();
        setDeletionAvailable(false);
    }

    private void refreshEvent(LaborCostForm.RefreshEvent event) {
        if (editingForm.isOpened()) {
            var rep = (LaborCostRepresentation) event.getItem();
            try {
                var item = listService.get(rep);
                editingForm.read(item);
            } catch (Throwable error) {
                showDialog(error);
            }
        } else
            this.grid.getDataProvider().refreshAll();
    }

    private void saveEvent(LaborCostForm.SaveEvent event) {
        var item = event.getItem();
        saveAndClose(item, false);
    }

    private void saveAndCloseEvent(LaborCostForm.SaveAndCloseEvent event) {
        var item = event.getItem();
        saveAndClose(item, true);
    }

    private void saveAndClose(Object item, boolean close) {
        if (item instanceof LaborCost castItem) {
            try {
                var savedItem = listService.save(castItem);
                if (close) {
                    closeEditor();
                    return;
                }
                if (editingForm.isOpened())
                    editingForm.read(savedItem);
            } catch (Throwable error) {
                showDialog(error);
            }
        }
    }

    private void closeEditor() {
        editingForm.close();
        setDeletionAvailable(true);
        this.grid.getDataProvider().refreshAll();
    }

}
