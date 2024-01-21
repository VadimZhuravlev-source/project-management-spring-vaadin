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
        editingForm.addListener(TimeUnitForm.SaveEvent.class, this::saveEvent);
        editingForm.addListener(TimeUnitForm.CloseEvent.class, closeEvent -> closeEditor());
        editingForm.open();
        setDeletionAvailable(false);
    }

    private void saveEvent(TimeUnitForm.SaveEvent event) {
        editingForm.close();
        var item = event.getItem();
        if (item instanceof TimeUnit timeUnit)
            listService.save(timeUnit);
        this.grid.getDataProvider().refreshAll();
    }

    private void closeEditor() {
        editingForm.close();
        setDeletionAvailable(true);
        this.grid.getDataProvider().refreshAll();
    }

}
