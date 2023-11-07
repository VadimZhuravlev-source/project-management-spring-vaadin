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

import javax.annotation.security.PermitAll;

@Route(value = "calendars", layout = MainLayout.class)
@PageTitle("Calendars | PM")
@PermitAll
public class CalendarsView extends VerticalLayout {

    private final CalendarService calendarService;
    private final ItemList<CalendarRepresentation, Calendar> list;
    private final CalendarFormNew calendarFormNew;
    private CalendarFormNew editingForm;

    public CalendarsView(CalendarService calendarService, CalendarFormNew calendarFormNew) {

        this.calendarService = calendarService;
        this.calendarFormNew = calendarFormNew;
        if (calendarService instanceof ListService) list = new Table((ListService) calendarService);
        else list = null;
        if (list != null) {
            //configureGrid();
            add(list);
            return;
        }

    }

    private class Table extends ItemList<CalendarRepresentation, Calendar> {

        Table(ListService listService) {
            super(listService);
            configureGrid();
        }

        private void configureGrid() {

            this.grid.addColumn(CalendarRepresentation::getName).setHeader("Name");
            this.grid.addColumn(CalendarRepresentation::getSettings).setHeader("Setting");
            this.grid.addColumn(CalendarRepresentation::getStartTime).setHeader("Start time");
            addPredefinedColumn();
            onMouseDoubleClick(this::openNewCalendar);

            beforeAddition(this::openNewCalendar);
            onCoping(this::openNewCalendar);

        }

        private void openNewCalendar(Calendar calendar) {
            openEditingForm(calendar);
        }

        private void openEditingForm(Calendar calendar) {
            editingForm = calendarFormNew.newInstance();
            editingForm.read(calendar);
            editingForm.addListener(CalendarFormNew.SaveEvent.class, this::saveEvent);
            editingForm.addListener(CalendarFormNew.CloseEvent.class, closeEvent -> closeEditor());
            editingForm.open();
            setDeletionAvailable(false);
        }

        private void saveEvent(CalendarFormNew.SaveEvent event) {
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