package com.pmvaadin.terms.calendars.view;

import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarRepresentation;
import com.pmvaadin.terms.calendars.entity.CalendarSettings;
import com.pmvaadin.terms.calendars.exceptiondays.ExceptionDays;
import com.pmvaadin.terms.calendars.services.CalendarService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.ArrayList;

@SpringComponent
public class CalendarFormNew extends Dialog {

    private CalendarRepresentation calendarRep;
    private Calendar calendar;
    private final CalendarService calendarService;

    private final TextField name = new TextField();
    private final ComboBox<CalendarSettings> setting = new ComboBox<>();
    private final TimePicker startTime = new TimePicker();

    private final NumberField monday = new NumberField();
    private final NumberField tuesday = new NumberField();
    private final NumberField wednesday = new NumberField();
    private final NumberField thursday = new NumberField();
    private final NumberField friday = new NumberField();
    private final NumberField saturday = new NumberField();
    private final NumberField sunday = new NumberField();

    private final Grid<ExceptionDays> exceptions = new Grid<>();

    private final Binder<Calendar> binder = new BeanValidationBinder<>(Calendar.class);

    public CalendarFormNew(CalendarService calendarService) {

        this.calendarService = calendarService;
        customizeHeader();
        this.setting.setItems(CalendarSettings.values());
        customizeForm();
        customizeDataLayout();
        customizeExceptions();
        createButtons();
        binder.bindInstanceFields(this);

    }

    public CalendarFormNew newInstance() {
        return new CalendarFormNew(calendarService);
    }

    public void read(CalendarRepresentation calendarRep) {
        this.calendarRep = calendarRep;
        this.calendar = calendarService.getCalendar(this.calendarRep);
        binder.readBean(this.calendar);
        fillExceptions();
        refreshHeader();
        binder.refreshFields();
    }

    private void fillExceptions() {

        this.exceptions.setItems(new ArrayList<>());
        var exceptions = this.calendar.getCalendarException();
        if (exceptions != null && !exceptions.isEmpty()) this.exceptions.setItems(exceptions);

    }

    private void customizeExceptions() {
        exceptions.addColumn(ExceptionDays::getDate).setHeader("Day");
        exceptions.addColumn(ExceptionDays::getDuration).setHeader("Hours");
    }

    private void customizeForm() {

        setDraggable(true);
        setResizable(true);
        addClassName("calendar-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);
        setCloseOnEsc(false);
        //this.addListener(Class<ProjectTaskForm>, )

    }

    private void customizeDataLayout() {

        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, "Name");
        formLayout.addFormItem(setting, "Setting");
        formLayout.addFormItem(startTime, "Start time");
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 3));

        FormLayout workingDaysLayout = new FormLayout();
        workingDaysLayout.addFormItem(monday, "Monday");
        workingDaysLayout.addFormItem(tuesday, "Tuesday");
        workingDaysLayout.addFormItem(wednesday, "Wednesday");
        workingDaysLayout.addFormItem(thursday, "Thursday");
        workingDaysLayout.addFormItem(friday, "Friday");
        workingDaysLayout.addFormItem(saturday, "Saturday");
        workingDaysLayout.addFormItem(sunday, "Sunday");

        VerticalLayout verticalLayout = new VerticalLayout(formLayout, workingDaysLayout);
        add(formLayout, verticalLayout, exceptions);

    }

    private void refreshHeader() {
        var calendarName = calendar.getName();
        if (calendarName == null) calendarName = "";
        var title = Calendar.getHeaderName();
        setHeaderTitle(title + ": " + calendarName);
    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> fireEvent(new CloseEvent(this))
        );
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickShortcut(Key.ESCAPE);

        getHeader().add(closeButton);

    }

    private void createButtons() {

        Button saveAndClose = new Button("Save and close");
        saveAndClose.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveAndClose.addClickListener(event -> {

            boolean validationDone = validateAndSave();
            if (!validationDone) return;
            fireEvent(new SaveEvent(this));

        });
        binder.addStatusChangeListener(e -> saveAndClose.setEnabled(binder.isValid()));

        Button sync = new Button("Refresh", new Icon("lumo", "reload"));
        sync.addClickListener(event -> syncData());
        sync.getStyle().set("margin-right", "auto");

        Button close = new Button("Close");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        Button save = new Button("Save");
        save.addClickListener(event -> validateAndSave());
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        getFooter().add(saveAndClose, save, sync, close);

    }

    private boolean validateAndSave() {
        try {
            binder.writeBean(this.calendar);
        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
            return false;
        }

        return true;
    }

    private void syncData() {
        read(this.calendarRep);
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class CalendarFormEvent extends ComponentEvent<CalendarFormNew> {

        protected CalendarFormEvent(CalendarFormNew source) {
            super(source, false);
        }

    }

    public static class CloseEvent extends CalendarFormEvent {
        CloseEvent(CalendarFormNew source) {
            super(source);
        }
    }

    public static class SaveEvent extends CalendarFormEvent {
        SaveEvent(CalendarFormNew source) {
            super(source);
        }
    }

}
