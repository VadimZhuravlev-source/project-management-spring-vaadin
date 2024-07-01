package com.pmvaadin.terms.timeunit.frontend.elements;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitRepresentation;
import com.pmvaadin.terms.timeunit.frontend.views.TimeUnitForm;

public class TimeUnitList extends ItemList<TimeUnitRepresentation, TimeUnit> {

    private final ListService<TimeUnitRepresentation, TimeUnit> listService;
    private TimeUnitForm editingForm;

    public TimeUnitList(ListService<TimeUnitRepresentation, TimeUnit> listService) {
        super(listService);
        this.listService = listService;
        configureGrid();
    }

    private void configureGrid() {

        this.grid.addColumn(TimeUnitRepresentation::getName).setHeader("Name");
        this.grid.addColumn(TimeUnitRepresentation::getNumberOfHours).setHeader("Hours");
        this.addFlagColumn(TimeUnitRepresentation::isPredefined);
        onMouseDoubleClick(this::openNewItem);
        beforeAddition(this::openNewItem);
        onCoping(this::openNewItem);
        onContextMenuOpen(this::openNewItem);
    }

    private void openNewItem(TimeUnit timeUnit) {
        openEditingForm(timeUnit);
    }

    private void openEditingForm(TimeUnit timeUnit) {
        editingForm = new TimeUnitForm();
        editingForm.read(timeUnit);
        editingForm.addListener(TimeUnitForm.SaveEvent.class, event -> saveAndClose(event.getItem(), false));
        editingForm.addListener(TimeUnitForm.CloseEvent.class, closeEvent -> closeEditor());
        editingForm.addListener(TimeUnitForm.SaveAndCloseEvent.class, event -> saveAndClose(event.getItem(), true));
        editingForm.open();
        setDeletionAvailable(false);
    }

    private void closeEditor() {
        editingForm.close();
        setDeletionAvailable(true);
        this.grid.getDataProvider().refreshAll();
    }

    private void saveAndClose(Object item, boolean close) {
        if (item instanceof TimeUnit castItem) {
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

}
