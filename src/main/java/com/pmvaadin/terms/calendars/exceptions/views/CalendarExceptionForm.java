package com.pmvaadin.terms.calendars.exceptions.views;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.IntervalGrid;
import com.pmvaadin.terms.calendars.exceptions.*;
import com.pmvaadin.terms.calendars.validators.Validation;
import com.pmvaadin.terms.calendars.validators.ValidationImpl;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;

import java.time.DayOfWeek;
import java.time.Month;

public class CalendarExceptionForm extends Dialog {

    private final CalendarException calendarException;
    private final Binder<CalendarException> binder = new Binder<>();
    private final TextField name = new TextField();
    // working times fields
    private final RadioButtonGroup<CalendarExceptionSetting> settingRadioButton = new RadioButtonGroup<>();
    private final Intervals intervals = new Intervals();
    // recurrence fields
    private final RadioButtonGroup<RecurrencePattern> recurrencePattern = new RadioButtonGroup<>();
    private final HorizontalLayout recurrencePatternContent = new HorizontalLayout();

    private final NumberField numberOfDays = new NumberField();
    private final NumberField numberOfWeeks = new NumberField();
    private final Checkbox everyMonday = new Checkbox("Monday");
    private final Checkbox everyTuesday = new Checkbox("Tuesday");
    private final Checkbox everyWednesday = new Checkbox("Wednesday");
    private final Checkbox everyThursday = new Checkbox("Thursday");
    private final Checkbox everyFriday = new Checkbox("Friday");
    private final Checkbox everySaturday = new Checkbox("Saturday");
    private final Checkbox everySunday = new Checkbox("Sunday");

    private final RadioButtonGroup<MonthlyPattern> monthlyPattern = new RadioButtonGroup<>();

    private final NumberField dayOfMonth = new NumberField();
    private final NumberField numberOfMonth = new NumberField();
    private final Select<NumberOfWeek> numberOfWeekThe = new Select<>();
    private final Select<DayOfWeek> dayOfWeekThe = new Select<>();
    private final NumberField numberOfMonthThe = new NumberField();

    private final RadioButtonGroup<YearlyPattern> yearlyPattern = new RadioButtonGroup<>();
    private final DatePicker onDate = new DatePicker();
    private final Select<NumberOfWeek> numberOfWeekYear = new Select<>();
    private final Select<DayOfWeek> dayOfWeekYear = new Select<>();
    private final Select<Month> monthYear = new Select<>();


    // range of occurrences fields
    private final DatePicker start = new DatePicker();
    private final RadioButtonGroup<RecurrenceEnd> endByAfter = new RadioButtonGroup<>();
    private final NumberField numberOfRecurrence = new NumberField();
    private final DatePicker finish = new DatePicker();

    private final Validation validation = new ValidationImpl();

    public CalendarExceptionForm(CalendarException calendarException) {
        this.calendarException = calendarException;
        customizeForm();
        customizeElements();
        customizeHeader();
        createButtons();
        refreshHeader();
        customizeBinder();
        intervals.setItems(this.calendarException.getIntervals());
        binder.readBean(this.calendarException);
    }

    private void customizeBinder() {
        binder.forField(settingRadioButton).bind(CalendarException::getSetting, CalendarException::setSetting);
        binder.forField(recurrencePattern).bind(CalendarException::getPattern, CalendarException::setPattern);
        binder.forField(endByAfter).bind(CalendarException::getEndByAfter, CalendarException::setEndByAfter);
        binder.forField(monthlyPattern).bind(CalendarException::getMonthlyPattern, CalendarException::setMonthlyPattern);
    }

    private void customizeForm() {

        setDraggable(true);
        setResizable(true);
        addClassName("calendar_exception-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);
        setCloseOnEsc(false);

        var mainLayout = new FormLayout();
        mainLayout.addFormItem(name, "Name");

        var intervalSetting = new VerticalLayout(settingRadioButton, intervals);
        intervalSetting.setWidthFull();
        var intervalSettingLayout = new FormLayout();
        intervalSettingLayout.addFormItem(intervalSetting, "Set working times for this exceptions");

        var recurrencePatternLayout = new HorizontalLayout(recurrencePattern, recurrencePatternContent);
        recurrencePatternLayout.setWidthFull();
        var recurrencePatternFormLayout = new FormLayout();
        recurrencePatternFormLayout.addFormItem(recurrencePatternLayout, "Recurrence pattern");

        // range of occurrences fields
        var rangeOfRecurrenceLayout = new HorizontalLayout();
        var start = new FormLayout();
        start.addFormItem(this.start, "Start");
        rangeOfRecurrenceLayout.add(start);

        rangeOfRecurrenceLayout.add(endByAfter);

        var rangeOfRecurrenceFormLayout = new FormLayout();
        rangeOfRecurrenceFormLayout.addFormItem(rangeOfRecurrenceLayout, "Range of recurrence");

        var verticalLayout = new VerticalLayout(mainLayout, intervalSettingLayout,
                recurrencePatternFormLayout, rangeOfRecurrenceFormLayout);
        verticalLayout.setWidthFull();

        add(verticalLayout);

    }

    private Component getDailyContent() {
        Text every = new Text("Every");
        Text days = new Text("days");
        return new HorizontalLayout(every, numberOfDays, days);
    }

    private Component getWeeklyContent() {
        Text every = new Text("Recur every");
        Text weeks = new Text(" week(s) on:");
        var horizontalLayout =  new HorizontalLayout(every, numberOfWeeks, weeks);
        var formLayout = new FormLayout(everyMonday, everyTuesday, everyWednesday, everyThursday, everyFriday,
                everySaturday, everySunday);
        return new VerticalLayout(horizontalLayout, formLayout);
    }

    private void customizeElements() {

        settingRadioButton.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        settingRadioButton.setItems(CalendarExceptionSetting.values());
        settingRadioButton.addValueChangeListener(this::settingRadioButtonChangeListener);

        recurrencePattern.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        recurrencePattern.setItems(RecurrencePattern.values());
        recurrencePattern.addValueChangeListener(this::recurrencePatternChangeListener);

        endByAfter.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        endByAfter.setItems(RecurrenceEnd.values());
        endByAfter.addValueChangeListener(this::endByAfterChangeListener);
        endByAfter.setRenderer(new ComponentRenderer<>(recurrenceEnd -> {
            Text text = new Text(recurrenceEnd.toString());
            var item = new HorizontalLayout(text);
            if (recurrenceEnd == RecurrenceEnd.AFTER) {
                var occurrences = new Text(" occurrences");
                item.add(numberOfRecurrence, occurrences);
            }
            else
                item.add(finish);
            return item;
        }));

        monthlyPattern.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        monthlyPattern.setItems(MonthlyPattern.values());
        monthlyPattern.setRenderer(new ComponentRenderer<>(monthlyPattern -> {
            var text = new Text(monthlyPattern.toString());
            var ofEvery = new Text("of every");
            var item = new HorizontalLayout(text);
            if (monthlyPattern == MonthlyPattern.DAY) {
                var months = new Text("month(s)");
                item.add(dayOfMonth, ofEvery, numberOfMonth, months);
            }
            else {
                var months = new Text("months");
                item.add(numberOfWeekThe, dayOfWeekThe, ofEvery, numberOfMonthThe, months);
            }
            return item;
        }));

        yearlyPattern.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        yearlyPattern.setItems(YearlyPattern.values());
        yearlyPattern.setRenderer(new ComponentRenderer<>(yearlyPattern -> {
            var text = new Text(yearlyPattern.toString());
            var item = new HorizontalLayout(text);
            if (yearlyPattern == YearlyPattern.ON) {
                item.add(onDate);
            }
            else {
                var of = new Text("of");
                item.add(numberOfWeekYear, dayOfWeekYear, of, monthYear);
            }
            return item;
        }));
    }

    private void endByAfterChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<RecurrenceEnd>, RecurrenceEnd> event) {
        numberOfRecurrence.setEnabled(event.getValue() == RecurrenceEnd.AFTER);
        finish.setEnabled(event.getValue() == RecurrenceEnd.BY);
    }

    private void settingRadioButtonChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<CalendarExceptionSetting>, CalendarExceptionSetting> event) {
        intervals.setEnabled(event.getValue() == CalendarExceptionSetting.WORKING_TIMES);
        if (event.getValue() == CalendarExceptionSetting.NONWORKING) intervals.clear();
        else intervals.setItems(this.calendarException.getDefaultIntervals());
    }

    private void recurrencePatternChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<RecurrencePattern>, RecurrencePattern> event) {

        var value = event.getValue();

        Component content = new Div();
        if (value == RecurrencePattern.DAILY)
            content = getDailyContent();
        else if (value == RecurrencePattern.WEEKLY)
            content = getWeeklyContent();
        else if (value == RecurrencePattern.MONTHLY) {
            content = monthlyPattern;
        } else if (value == RecurrencePattern.YEARLY) {
            content = yearlyPattern;
        }

        recurrencePatternContent.removeAll();
        recurrencePatternContent.add(content);

    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> {
                    fireEvent(new CalendarExceptionForm.CloseEvent(this, this.calendarException));
                    this.close();
                });
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickShortcut(Key.ESCAPE);

        getHeader().add(closeButton);

    }

    private void createButtons() {

        Button ok = new Button("Ok");
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ok.addClickListener(event -> {

            var validationDone = false;
            try {
                validationDone = validate();
            } catch (StandardError error) {
                var confDialog = new ConfirmDialog();
                confDialog.setText(error.getMessage());
                confDialog.open();
            }

            if (!validationDone) return;

            try {
                writeChanges();
            } catch (ValidationException error) {
                var confDialog = new ConfirmDialog();
                confDialog.setText(error.getMessage());
                confDialog.open();
                return;
            }
            fireEvent(new CalendarExceptionForm.SaveEvent(this, this.calendarException));
            close();

        });

        Button close = new Button("Cancel");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event ->
        {
            fireEvent(new CalendarExceptionForm.CloseEvent(this, this.calendarException));
            close();
        });

        getFooter().add(ok, close);

    }

    private boolean validate() {
        validation.validateIntervals(intervals.getItems());
        return true;
    }

    private void writeChanges() throws ValidationException {
        binder.writeBean(this.calendarException);
        this.calendarException.setIntervals(intervals.getItems());
    }

    private void refreshHeader() {
        var exceptionName = this.calendarException.getName();
        if (exceptionName == null) exceptionName = "";
        var title = CalendarException.getHeaderName();
        setHeaderTitle(title + exceptionName);
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class CalendarExceptionFormEvent extends ComponentEvent<CalendarExceptionForm> {

        private final CalendarException calendarException;

        protected CalendarExceptionFormEvent(CalendarExceptionForm source, CalendarException calendarException) {
            super(source, false);
            this.calendarException = calendarException;
        }

        public CalendarException getCalendarException() {
            return this.calendarException;
        }

    }

    public static class CloseEvent extends CalendarExceptionFormEvent {
        CloseEvent(CalendarExceptionForm source, CalendarException calendarException) {
            super(source, calendarException);
        }
    }

    public static class SaveEvent extends CalendarExceptionFormEvent {
        SaveEvent(CalendarExceptionForm source, CalendarException calendarException) {
            super(source, calendarException);
        }
    }

    private static class Intervals extends IntervalGrid {

        public void clear() {
            grid.getListDataView().removeItems(grid.getListDataView().getItems().toList());
        }

    }

}
