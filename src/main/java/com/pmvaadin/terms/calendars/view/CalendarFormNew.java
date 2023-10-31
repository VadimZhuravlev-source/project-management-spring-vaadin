package com.pmvaadin.terms.calendars.view;

import com.pmvaadin.projectstructure.NotificationDialogs;
import com.pmvaadin.terms.calendars.dayofweeksettings.DayOfWeekSettings;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.time.DayOfWeek;
import java.util.ArrayList;

@SpringComponent
public class CalendarFormNew extends Dialog {

    private CalendarRepresentation calendarRep;
    private Calendar calendar;
    private final CalendarService calendarService;

    private final TextField name = new TextField("Name");
    private final ComboBox<CalendarSettings> setting = new ComboBox<>("Setting");
    private final TimePicker startTime = new TimePicker("Start time");

    private final NumberField monday = new NumberField("Monday");
    private final NumberField tuesday = new NumberField("Tuesday");
    private final NumberField wednesday = new NumberField("Wednesday");
    private final NumberField thursday = new NumberField("Thursday");
    private final NumberField friday = new NumberField("Friday");
    private final NumberField saturday = new NumberField("Saturday");
    private final NumberField sunday = new NumberField("Sunday");

    private final Grid<ExceptionDays> exceptions = new Grid<>();

    private final Binder<Calendar> binder = new BeanValidationBinder<>(Calendar.class);

    private final Button sync = new Button("Refresh", new Icon("lumo", "reload"));

    public CalendarFormNew(CalendarService calendarService) {

        this.calendarService = calendarService;
        customizeHeader();
        this.setting.setItems(CalendarSettings.values());
        customizeForm();
        customizeDataLayout();
        customizeExceptions();
        createButtons();
        binder.bindInstanceFields(this);
        binder.forField(monday).bind(c -> getHoursInTheDay(DayOfWeek.MONDAY), (c, aDouble) -> setHoursInTheDay(DayOfWeek.MONDAY, aDouble));
        binder.forField(tuesday).bind(c -> getHoursInTheDay(DayOfWeek.TUESDAY), (c, aDouble) -> setHoursInTheDay(DayOfWeek.TUESDAY, aDouble));
        binder.forField(wednesday).bind(c -> getHoursInTheDay(DayOfWeek.WEDNESDAY), (c, aDouble) -> setHoursInTheDay(DayOfWeek.WEDNESDAY, aDouble));
        binder.forField(thursday).bind(c -> getHoursInTheDay(DayOfWeek.THURSDAY), (c, aDouble) -> setHoursInTheDay(DayOfWeek.THURSDAY, aDouble));
        binder.forField(friday).bind(c -> getHoursInTheDay(DayOfWeek.FRIDAY), (c, aDouble) -> setHoursInTheDay(DayOfWeek.FRIDAY, aDouble));
        binder.forField(saturday).bind(c -> getHoursInTheDay(DayOfWeek.SATURDAY), (c, aDouble) -> setHoursInTheDay(DayOfWeek.SATURDAY, aDouble));
        binder.forField(sunday).bind(c -> getHoursInTheDay(DayOfWeek.SUNDAY), (c, aDouble) -> setHoursInTheDay(DayOfWeek.SUNDAY, aDouble));

    }

    public CalendarFormNew newInstance() {
        return new CalendarFormNew(calendarService);
    }

    public void read(CalendarRepresentation calendarRep) {

        this.calendarRep = calendarRep;
        this.calendar = calendarService.getCalendar(this.calendarRep);

        read();

    }

    public void read(Calendar calendar) {

        this.calendar = calendar;
        read();

    }

    private Double getHoursInTheDay(DayOfWeek dayOfWeek) {
        var dayIndex = dayOfWeek.getValue();
        var daysOfWeek = this.calendar.getDaysOfWeekSettings();
        if (daysOfWeek == null || daysOfWeek.isEmpty()) return 0D;
        return daysOfWeek.stream().filter(d -> d.getDayOfWeek() == dayIndex)
                .map(DayOfWeekSettings::getCountHours).map(v -> ((double) v) / Calendar.NUMBER_OF_SECONDS_IN_AN_HOUR)
                .findFirst().orElse(0D);
    }

    private void setHoursInTheDay(DayOfWeek dayOfWeek, Double aDouble) {

        var dayIndex = dayOfWeek.getValue();
        var daysOfWeek = this.calendar.getDaysOfWeekSettings();
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            daysOfWeek = this.calendar.getSetting().getDaysOfWeekSettings();
            this.calendar.setDaysOfWeekSettings(daysOfWeek);
        }

        var dayOfWeekOptional = daysOfWeek.stream().filter(d -> d.getDayOfWeek() == dayIndex).findFirst();
        var countOfHours = (int) (aDouble * Calendar.DAY_DURATION_SECONDS);

        DayOfWeekSettings currentDayOfWeek = null;

        if (dayOfWeekOptional.isEmpty()) {
            currentDayOfWeek = new DayOfWeekSettings(dayIndex, countOfHours);
            daysOfWeek.add(currentDayOfWeek);
        } else {
            currentDayOfWeek = dayOfWeekOptional.get();
            currentDayOfWeek.setCountHours(countOfHours);
        }

    }

    private void read() {

        binder.readBean(this.calendar);
        fillExceptions();
        refreshHeader();

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

        var mainLayout = new VerticalLayout(name, setting, startTime);

//        FormLayout formLayout = new FormLayout();
//        formLayout.addFormItem(name, "Name");
//        formLayout.addFormItem(setting, "Setting");
//        formLayout.addFormItem(startTime, "Start time");
//        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
//                new FormLayout.ResponsiveStep("300px", 3));

        var workingDaysLayout = new VerticalLayout(monday, tuesday, wednesday, thursday, friday, saturday, sunday);

//        FormLayout workingDaysLayout = new FormLayout();
//        workingDaysLayout.addFormItem(monday, "Monday");
//        workingDaysLayout.addFormItem(tuesday, "Tuesday");
//        workingDaysLayout.addFormItem(wednesday, "Wednesday");
//        workingDaysLayout.addFormItem(thursday, "Thursday");
//        workingDaysLayout.addFormItem(friday, "Friday");
//        workingDaysLayout.addFormItem(saturday, "Saturday");
//        workingDaysLayout.addFormItem(sunday, "Sunday");

        var horizontalLayout = new HorizontalLayout(mainLayout, workingDaysLayout);

        add(horizontalLayout, exceptions);

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
            calendarService.saveCalendars(calendar);
        } catch (Throwable e) {
            NotificationDialogs.notifyValidationErrors(e.getMessage());
            return false;
        }

        return true;

    }

    private void syncData() {
        if (this.calendarRep == null) return;
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
