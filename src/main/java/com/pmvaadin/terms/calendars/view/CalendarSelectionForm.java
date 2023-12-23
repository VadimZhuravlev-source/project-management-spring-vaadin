package com.pmvaadin.terms.calendars.view;

import com.pmvaadin.commonobjects.services.ItemService;
import com.pmvaadin.commonobjects.vaadin.SearchableGrid;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarRepresentation;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.function.Consumer;

@SpringComponent
public class CalendarSelectionForm extends Dialog {
    private final CalendarService calendarService;

    //private final Button selectionButton = new Button("Select");
    private final SelectionGrid selectionGrid;

    private Consumer<Calendar> selection;

    public CalendarSelectionForm(CalendarService calendarService) {

        this.calendarService = calendarService;
        if (!(calendarService instanceof ItemService)) {
            selectionGrid = null;
            return;
        }
        selectionGrid = new SelectionGrid((ItemService<CalendarRepresentation>) calendarService);

        var selectionButton = new Button("Select");
        selectionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(selectionButton);
        selectionButton.getStyle().set("margin-right", "auto");
//        customizeGrid();
        customizeHeader();
        add(selectionGrid);

        setSizeFull();
        //grid.setSizeFull();
        setDraggable(true);
        setResizable(true);
        addClassName("dialog-padding-1");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        selectionButton.addClickListener(event -> {
            var calendar = selectionGrid.getSelectedItem();
            selectItem(calendar);
        });
    }

    public CalendarSelectionForm newInstance() {
        return new CalendarSelectionForm(calendarService);
    }

    public void addSelectionListener(Consumer<Calendar> selection) {
        this.selection = selection;
    }

    private void customizeHeader() {

        setHeaderTitle("Calendar selection");
        var closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

    }

    private void selectItem(CalendarRepresentation calendar) {
        if (calendar == null) return;
        var selectedCalendar = calendarService.getCalendarById(calendar.getId());
        if (selection != null) selection.accept(selectedCalendar);
        close();
    }

    private class SelectionGrid extends SearchableGrid<CalendarRepresentation> {

        SelectionGrid(ItemService<CalendarRepresentation> itemService) {
            super(itemService);
            this.grid.addColumn(CalendarRepresentation::getName).setHeader("Name");
            this.grid.addColumn(CalendarRepresentation::getSettings).setHeader("Setting");
            this.addPredefinedColumn(CalendarRepresentation::isPredefined);
            this.grid.addItemDoubleClickListener(event -> {
                if (event == null) return;
                CalendarRepresentation calendar = event.getItem();
                selectItem(calendar);
            });
        }

        CalendarRepresentation getSelectedItem() {

            return this.grid.getSelectedItems().stream().findFirst().orElse(null);

        }

    }

}
