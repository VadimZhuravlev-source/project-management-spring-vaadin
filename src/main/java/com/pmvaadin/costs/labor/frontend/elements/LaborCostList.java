package com.pmvaadin.costs.labor.frontend.elements;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentation;
import com.pmvaadin.costs.labor.frontend.views.LaborCostForm;
import com.pmvaadin.costs.labor.services.LaborCostService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.resources.labor.frontend.elements.FilteredLaborResourceComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.data.binder.ValidationException;

public class LaborCostList extends ItemList<LaborCostRepresentation, LaborCost> {

    private final LaborCostService listService;
    private final FilteredLaborResourceComboBox resourceComboBox;
    private LaborCostForm editingForm;

    public LaborCostList(LaborCostService listService, FilteredLaborResourceComboBox resourceComboBox) {
        super((ListService<LaborCostRepresentation, LaborCost>) listService);
        this.listService = listService;
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
        editingForm = new LaborCostForm(listService, resourceComboBox);
        editingForm.read(laborCost);
        editingForm.addListener(LaborCostForm.SaveEvent.class, this::saveEvent);
        editingForm.addListener(LaborCostForm.SaveAndCloseEvent.class, this::saveAndCloseEvent);
        editingForm.addListener(LaborCostForm.CloseEvent.class, event -> closeEditor());
        editingForm.addListener(DialogForm.RefreshEvent.class, event -> {
            if (editingForm.isOpened()) {
                var rep = (LaborCostRepresentation) event.getItem();
                var item = ((ListService<LaborCostRepresentation, LaborCost>) listService).get(rep);
                editingForm.read(item);
            } else
                this.grid.getDataProvider().refreshAll();
        });
        editingForm.open();
        setDeletionAvailable(false);
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
        if (item instanceof LaborCost laborCost) {
            if (editingForm.isOpened()) {
                try {
                    var savedResource = ((ListService<LaborCostRepresentation, LaborCost>) listService).save(laborCost);
                    if (close) {
                        editingForm.close();
                        return;
                    }
                    editingForm.read(savedResource);
                } catch (StandardError error) {
                    var confDialog = new ConfirmDialog();
                    confDialog.setText(error.getMessage());
                    confDialog.open();
                    return;
                }
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
