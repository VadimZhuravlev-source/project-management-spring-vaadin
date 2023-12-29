package com.pmvaadin.terms.calendars.view;

import com.pmvaadin.commonobjects.services.ListService;
import com.pmvaadin.commonobjects.vaadin.ItemList;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarRepresentation;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.pmvaadin.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "calendars", layout = MainLayout.class)
@PageTitle("Calendars | PM")
@PermitAll
public class CalendarsView extends VerticalLayout {

    private final CalendarService calendarService;
    private final ItemList<CalendarRepresentation, Calendar> list;
    private final CalendarForm calendarForm;
    private CalendarForm editingForm;

    public CalendarsView(CalendarService calendarService, CalendarForm calendarForm) {

        this.calendarService = calendarService;
        this.calendarForm = calendarForm;
        if (!(calendarService instanceof ListService)) {
            list = null;
            return;
        }

        list = new Table((ListService) calendarService);
        add(list);

    }

    private class Table extends ItemList<CalendarRepresentation, Calendar> {

        Table(ListService<CalendarRepresentation, Calendar> listService) {
            super(listService);
            configureGrid();
        }

        private void configureGrid() {

            this.grid.addColumn(CalendarRepresentation::getName).setHeader("Name");
            this.grid.addColumn(CalendarRepresentation::getSettings).setHeader("Setting");
            this.addPredefinedColumn(CalendarRepresentation::isPredefined);
            onMouseDoubleClick(this::openNewCalendar);

            beforeAddition(this::openNewCalendar);
            onCoping(this::openNewCalendar);

        }

        private void openNewCalendar(Calendar calendar) {
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
            editingForm.close();
            this.grid.getDataProvider().refreshAll();
        }

        private void closeEditor() {
            editingForm.close();
            setDeletionAvailable(true);
            this.grid.getDataProvider().refreshAll();
        }

    }

}