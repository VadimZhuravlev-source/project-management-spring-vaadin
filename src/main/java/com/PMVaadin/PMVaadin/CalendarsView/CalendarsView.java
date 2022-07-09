package com.PMVaadin.PMVaadin.CalendarsView;

import com.PMVaadin.PMVaadin.Entities.Calendar.CalendarRowTable;
import com.PMVaadin.PMVaadin.MainLayout;
import com.PMVaadin.PMVaadin.Services.CalendarService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.PermitAll;
import java.util.List;

@Route(value="calendars", layout = MainLayout.class)
@PageTitle("Calendars | PM")
@PermitAll
public class CalendarsView extends VerticalLayout {

    private CalendarService calendarService;
    private Grid<CalendarRowTable> grid = new Grid<>(CalendarRowTable.class);
    private TextField filterText = new TextField();

    public CalendarsView(CalendarService calendarService) {

        this.calendarService = calendarService;
        addClassName("calendar-list-view");
        setSizeFull();
        configureGrid();

        populateDate();

        add(getToolbar(), grid);
    }

    private void configureGrid() {
        grid.addClassNames("calendar-grid");
        grid.setSizeFull();
        //grid.setColumns("name", "setting");
        grid.addColumn(CalendarRowTable -> CalendarRowTable.getName()).setHeader("Name");
        grid.addColumn(CalendarRowTable -> CalendarRowTable.getSetting().toString()).setHeader("Setting");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);

        Button addContactButton = new Button("Add calendar");

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addContactButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void populateDate() {
        List<CalendarRowTable> calendars = calendarService.getCalendars();
        grid.setItems(calendars);
    }


}
