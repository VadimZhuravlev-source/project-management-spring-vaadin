package com.PMVaadin.PMVaadin.CalendarsView;

import com.PMVaadin.PMVaadin.Entities.calendar.*;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
public class CalendarForm extends FormLayout {
    private CalendarImpl calendar;
    private List<ExceptionDays> exceptionDaysList;
    private List<DayOfWeekSettings> workDaysList;

    private DatePicker dateOfException = new DatePicker(ExceptionDays.getExceptionDaysName());
    private IntegerField dayOfWeek = new IntegerField(DayOfWeekSettings.getWorkDaysName());
    private BigDecimalField hourOfWork = new BigDecimalField(DayOfWeekSettings.getHourOfWorkName());
    private TextField calendarName = new TextField(CalendarImpl.getHeaderName());
    private ComboBox<CalendarSettings> calendarSetting = new ComboBox<CalendarSettings>(CalendarImpl.getSettingName());
    private Grid<ExceptionDays> exceptionDaysGrid = new Grid<>(ExceptionDays.class, false);
    private Grid<DayOfWeekSettings> workDaysGrid = new Grid<>(DayOfWeekSettings.class, false);

    private Binder<Calendar> binder = new BeanValidationBinder<>(Calendar.class);
    private Binder<ExceptionDays> binderExceptionDays = new BeanValidationBinder<>(ExceptionDays.class);
    private Binder<DayOfWeekSettings> binderWorkDays = new BeanValidationBinder<>(DayOfWeekSettings.class);


    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");
    private final Button addException = new Button("Add exception day");
    private final Button deleteException = new Button("Delete exception day");
    private final Button addDayOfWeek = new Button("Add day of week");
    private final Button deleteDayOfWeek = new Button("Delete day of week");
    private final Button fillDayOfWeek = new Button("Fill days of week");

    public CalendarForm() {
        if (calendar == null) calendar = new CalendarImpl();
        exceptionDaysList = calendar.getCalendarException();
        if (null == exceptionDaysList) exceptionDaysList = new ArrayList<>();
        workDaysList = calendar.getDaysOfWeekSettings();
        if (null == workDaysList) workDaysList = new ArrayList<>();
        calendarSetting.setItems(CalendarSettings.values());
        add(
                calendarName,
                calendarSetting,
                createExceptionsTable(),
                createExceptionButtonsLayout(),
                createWorkDaysTable(),
                createDayButtonsLayout(),
                createButtonsLayout())
        ;
        calendarName.setAutofocus(true);
    }

    private Grid<ExceptionDays> createExceptionsTable() {
        Editor<ExceptionDays> editor = exceptionDaysGrid.getEditor();
        Grid.Column<ExceptionDays> dateColumn = exceptionDaysGrid
                .addColumn(ExceptionDays::getDate).setHeader("Date")
                .setWidth("120px").setFlexGrow(0);
        Grid.Column<ExceptionDays> editColumn = exceptionDaysGrid.addComponentColumn(day -> {
            Button editButton = new Button("Edit");
            editButton.addClickListener(e -> {
                if (editor.isOpen())
                    editor.cancel();
                exceptionDaysGrid.getEditor().editItem(day);
            });
            return editButton;
        }).setWidth("150px").setFlexGrow(0);
        editor.setBinder(binderExceptionDays);
        editor.setBuffered(true);

        binderExceptionDays.forField(dateOfException).bind(ExceptionDays::getDate, ExceptionDays::setDate);
        dateColumn.setEditorComponent(dateOfException);
        Button saveButton = new Button("Save", e -> editor.save());
        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                e -> editor.cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR);
        HorizontalLayout actions = new HorizontalLayout(saveButton,
                cancelButton);
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);
        exceptionDaysGrid.setItems(exceptionDaysList);


        return exceptionDaysGrid;
    }

    private Grid<DayOfWeekSettings> createWorkDaysTable() {
        Editor<DayOfWeekSettings> editor = workDaysGrid.getEditor();
        Grid.Column<DayOfWeekSettings> dayOfWeekColumn = workDaysGrid
                .addColumn(DayOfWeekSettings::getDayOfWeek).setHeader("Day")
                .setWidth("120px").setFlexGrow(0);
        Grid.Column<DayOfWeekSettings> countHoursColumn = workDaysGrid
                .addColumn(DayOfWeekSettings::getCountHours).setHeader("Hours")
                .setWidth("120px").setFlexGrow(0);
        Grid.Column<DayOfWeekSettings> editColumn = workDaysGrid.addComponentColumn(day -> {
            Button editButton = new Button("Edit");
            editButton.addClickListener(e -> {
                if (editor.isOpen())
                    editor.cancel();
                workDaysGrid.getEditor().editItem(day);
            });
            return editButton;
        }).setWidth("150px").setFlexGrow(0);
        editor.setBinder(binderWorkDays);
        editor.setBuffered(true);

        binderWorkDays.forField(dayOfWeek).bind(DayOfWeekSettings::getDayOfWeek, DayOfWeekSettings::setDayOfWeek);
        dayOfWeekColumn.setEditorComponent(dayOfWeek);
        binderWorkDays.forField(hourOfWork).bind(DayOfWeekSettings::getCountHours, DayOfWeekSettings::setCountHours);
        countHoursColumn.setEditorComponent(hourOfWork);
        Button saveButton = new Button("Save", e -> editor.save());
        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                e -> editor.cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR);
        HorizontalLayout actions = new HorizontalLayout(saveButton,
                cancelButton);
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);
        workDaysGrid.setItems(workDaysList);


        return workDaysGrid;
    }

    private HorizontalLayout createDayButtonsLayout() {
        addDayOfWeek.addClickListener(e -> addDayOfWeek());
        deleteDayOfWeek.addClickListener(e -> deleteDayOfWeek());
        fillDayOfWeek.addClickListener(e -> fillDayOfWeek());
        return new HorizontalLayout(addDayOfWeek, deleteDayOfWeek, fillDayOfWeek);
    }

    private HorizontalLayout createExceptionButtonsLayout() {
        addException.addClickListener(e -> addExceptionDate());
        deleteException.addClickListener(e -> deleteExceptionDate());
        return new HorizontalLayout(addException, deleteException);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        save.addClickShortcut(Key.ENTER);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new CalendarForm.DeleteEvent(this, calendar)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete);
    }

    public void setCalendar(CalendarImpl calendar) {
        this.calendar = calendar;
        calendarName.setValue(calendar.getId() == null ? "" : calendar.getName());
        calendarSetting.setValue(calendar.getId() == null ? CalendarSettings.EIGHTHOURWORKINGDAY : calendar.getSetting());
        exceptionDaysGrid.setItems(calendar.getId() == null ? new ArrayList<>() : calendar.getCalendarException());
        workDaysGrid.setItems(calendar.getId() == null ? new ArrayList<>() : calendar.getDaysOfWeekSettings());
        binder.readBean(calendar);
        calendarName.focus();
    }

    private void addExceptionDate() {
        ExceptionDays exceptionDay = new ExceptionDays();
        exceptionDay.setCalendar(calendar);
        exceptionDaysList.add(exceptionDay);
        exceptionDaysGrid.setItems(exceptionDaysList);
    }

    private void deleteExceptionDate() {
        ExceptionDays exceptionDay = (ExceptionDays) exceptionDaysGrid.getSelectionModel().getSelectedItems()
                .stream().findFirst().orElseThrow();
        exceptionDaysList.remove(exceptionDay);
        exceptionDaysGrid.setItems(exceptionDaysList);
    }

    private void addDayOfWeek() {
        DayOfWeekSettings dayOfWeek = new DayOfWeekSettings();
        dayOfWeek.setCalendar(calendar);
        workDaysList.add(dayOfWeek);
        workDaysGrid.setItems(workDaysList);
    }

    private void deleteDayOfWeek() {
        DayOfWeekSettings weekDay = (DayOfWeekSettings) workDaysGrid.getSelectionModel().getSelectedItems()
                .stream().findFirst().orElseThrow();
        workDaysList.remove(weekDay);
        workDaysGrid.setItems(workDaysList);
    }

    private void fillDayOfWeek() {
        CalendarSettings value = calendarSetting.getValue();
        List<DayOfWeekSettings> daysOfWeekSettings = value.getDaysOfWeekSettings();
        daysOfWeekSettings.forEach(e -> e.setCalendar(calendar));
        workDaysList.addAll(daysOfWeekSettings);
        workDaysGrid.setItems(workDaysList);
    }

    private void validateAndSave() {
        try {
            calendar.setName(calendarName.getValue());
            calendar.setSetting(calendarSetting.getValue());
            calendar.setCalendarException(exceptionDaysList);
            calendar.setDaysOfWeekSettings(workDaysList);
            binder.writeBean(calendar);
            fireEvent(new CalendarForm.SaveEvent(this, calendar));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    // Events
    public static abstract class CalendarFormEvent extends ComponentEvent<CalendarForm> {
        private CalendarImpl calendar;

        protected CalendarFormEvent(CalendarForm source, CalendarImpl calendar) {
            super(source, false);
            this.calendar = calendar;
        }

        public CalendarImpl getCalendar() {
            return calendar;
        }
    }

    public static class EditEvent extends CalendarForm.CalendarFormEvent {
        EditEvent(CalendarForm source, CalendarImpl calendar) {
            super(source, calendar);
        }
    }

    public static class SaveEvent extends CalendarForm.CalendarFormEvent {
        SaveEvent(CalendarForm source, CalendarImpl calendar) {
            super(source, calendar);
        }
    }

    public static class DeleteEvent extends CalendarForm.CalendarFormEvent {
        DeleteEvent(CalendarForm source, CalendarImpl calendar) {
            super(source, calendar);
        }

    }

    public static class CloseEvent extends CalendarForm.CalendarFormEvent {
        CloseEvent(CalendarForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
