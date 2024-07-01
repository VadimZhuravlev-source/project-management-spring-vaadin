package com.pmvaadin.resources.labor.frontend.elements;

import com.pmvaadin.common.DialogForm;
import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.costs.labor.frontend.views.LaborCostForm;
import com.pmvaadin.resources.labor.entity.LaborResource;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.frontend.views.LaborResourceForm;

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
        onContextMenuOpen(this::openNewItem);
    }

    private void openNewItem(LaborResource laborResource) {
        openEditingForm(laborResource);
    }

    private void openEditingForm(LaborResource laborResource) {
        editingForm = new LaborResourceForm();
        editingForm.read(laborResource);
        editingForm.addListener(LaborResourceForm.SaveEvent.class, event -> saveAndClose(event.getItem(), false));
        editingForm.addListener(LaborResourceForm.CloseEvent.class, closeEvent -> closeEditor());
        editingForm.addListener(LaborResourceForm.SaveAndCloseEvent.class, event -> saveAndClose(event.getItem(), true));
        editingForm.addListener(DialogForm.RefreshEvent.class, this::refreshEvent);

        editingForm.open();
        setDeletionAvailable(false);
    }

    private void saveAndClose(Object item, boolean close) {
        if (item instanceof LaborResource castItem) {
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

    private void refreshEvent(LaborCostForm.RefreshEvent event) {
        if (editingForm.isOpened()) {
            var rep = (LaborResourceRepresentation) event.getItem();
            try {
                var item = listService.get(rep);
                editingForm.read(item);
            } catch (Throwable error) {
                showDialog(error);
            }
        } else
            this.grid.getDataProvider().refreshAll();
    }

}
