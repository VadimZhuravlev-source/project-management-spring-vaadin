package com.pmvaadin.calendars.view;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.calendars.services.CalendarService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.function.Consumer;

@SpringComponent
public class CalendarSelectionForm extends Dialog implements SelectionForm<Calendar>{
    private CalendarService calendarService;

    private final HorizontalLayout toolBar = new HorizontalLayout();
    private final Button selectionAction = new Button("Select");
    private final Grid<Calendar> grid = new Grid<>();

    private Consumer<Calendar> selection;

    public CalendarSelectionForm(CalendarService calendarService) {
        super();
        this.calendarService = calendarService;
        grid.setItems(calendarService.getCalendars());
        selectionAction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(selectionAction);
        selectionAction.getStyle().set("margin-right", "auto");
        customizeGrid();
        customizeHeader();
        add(grid);

        setSizeFull();
        grid.setSizeFull();
        setDraggable(true);
        setResizable(true);
        addClassName("calendar-selection-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        selectionAction.addClickListener(event -> {
            Calendar calendar = grid.getSelectedItems().stream().findFirst().orElse(null);
            selectItem(calendar);
        });
    }

    public void addSelectionListener(Consumer<Calendar> selection) {
        this.selection = selection;
    }

    private void customizeHeader() {

        setHeaderTitle("Calendar selection");
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

    }

    private void customizeGrid() {

        grid.addClassNames("calendar-selection-grid");
        grid.setSizeFull();
        grid.setColumnReorderingAllowed(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addColumn(Calendar::getName);
        grid.addColumn(Calendar::getSetting);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.addItemDoubleClickListener(event -> {
            if (event == null) return;
            Calendar calendar = event.getItem();
            selectItem(calendar);
        });

    }

    private void selectItem(Calendar calendar) {
        if (calendar == null) return;
        if (selection != null) selection.accept(calendar);
        close();
    }
}
