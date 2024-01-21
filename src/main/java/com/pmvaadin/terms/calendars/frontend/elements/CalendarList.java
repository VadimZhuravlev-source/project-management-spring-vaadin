package com.pmvaadin.terms.calendars.frontend.elements;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.common.vaadin.ItemList;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarRepresentation;
import com.pmvaadin.terms.calendars.frontend.view.CalendarForm;

public class CalendarList extends ItemList<CalendarRepresentation, Calendar> {

    private final ListService<CalendarRepresentation, Calendar> listService;
    private final CalendarForm calendarForm;
    private CalendarForm editingForm;

    public CalendarList(ListService<CalendarRepresentation, Calendar> listService, CalendarForm calendarForm) {
        super(listService);
        this.listService = listService;
        this.calendarForm = calendarForm;
        configureGrid();
    }

    private void configureGrid() {

        this.grid.addColumn(CalendarRepresentation::getName).setHeader("Name");
        this.grid.addColumn(CalendarRepresentation::getSetting).setHeader("Setting");
        this.addFlagColumn(CalendarRepresentation::isPredefined);
        onMouseDoubleClick(this::openNewItem);

        beforeAddition(this::openNewItem);
        onCoping(this::openNewItem);
        onContextMenuOpen(this::openNewItem);

    }

    private void openNewItem(Calendar calendar) {
        openEditingForm(calendar);
    }

    private void openEditingForm(Calendar calendar) {
        editingForm = calendarForm.newInstance();
        editingForm.read(calendar);
        editingForm.addListener(CalendarForm.SaveEvent.class, this::saveEvent);
        editingForm.addListener(CalendarForm.CloseEvent.class, closeEvent -> closeEditor());
        editingForm.open();
        setDeletionAvailable(false);
    }

    private void saveEvent(CalendarForm.SaveEvent event) {
        this.grid.getDataProvider().refreshAll();
    }

    private void closeEditor() {
        editingForm.close();
        setDeletionAvailable(true);
        this.grid.getDataProvider().refreshAll();
    }

}
