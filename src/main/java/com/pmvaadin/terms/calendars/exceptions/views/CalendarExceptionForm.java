package com.pmvaadin.terms.calendars.exceptions.views;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.Interval;
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
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.WeekFields;
import java.util.Comparator;

public class CalendarExceptionForm extends Dialog {

    private final CalendarException calendarException;
    private final Binder<CalendarException> binder = new Binder<>(CalendarException.class);
    private final TextField name = new TextField();
    // working times fields
    private final RadioButtonGroup<CalendarExceptionSetting> settingRadioButton = new RadioButtonGroup<>();
    private final Intervals intervals = new Intervals();
    // recurrence fields
    private final RadioButtonGroup<RecurrencePattern> recurrencePattern = new RadioButtonGroup<>();
    private final HorizontalLayout recurrencePatternContent = new HorizontalLayout();

    private final NumberField numberOfDays = getCustomizedNumberField();
    private final NumberField numberOfWeeks = getCustomizedNumberField();
    private final Checkbox everyMonday = new Checkbox("Monday");
    private final Checkbox everyTuesday = new Checkbox("Tuesday");
    private final Checkbox everyWednesday = new Checkbox("Wednesday");
    private final Checkbox everyThursday = new Checkbox("Thursday");
    private final Checkbox everyFriday = new Checkbox("Friday");
    private final Checkbox everySaturday = new Checkbox("Saturday");
    private final Checkbox everySunday = new Checkbox("Sunday");

    private final RadioButtonGroup<MonthlyPattern> monthlyPattern = new RadioButtonGroup<>();

    private final NumberField dayOfMonth = getCustomizedNumberField();
    private final NumberField numberOfMonth = getCustomizedNumberField();
    private final Select<NumberOfWeek> numberOfWeekThe = new Select<>();
    private final Select<DayOfWeek> dayOfWeekThe = new Select<>();
    private final NumberField numberOfMonthThe = getCustomizedNumberField();

    private final RadioButtonGroup<YearlyPattern> yearlyPattern = new RadioButtonGroup<>();
    private final NumberField onDateDay = getCustomizedNumberField();
    private final Select<Month> onDateMonth = new Select<>();
    private final Select<NumberOfWeek> numberOfWeekYear = new Select<>();
    private final Select<DayOfWeek> dayOfWeekYear = new Select<>();
    private final Select<Month> monthYear = new Select<>();


    // range of occurrences fields
    private final DatePicker start = new DatePicker();
    private final RadioButtonGroup<RecurrenceEnd> endByAfter = new RadioButtonGroup<>();
    private final NumberField numberOfOccurrence = getCustomizedNumberField();
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
        intervals.setItems(this.calendarException.getCopyOfIntervals());
        binder.readBean(this.calendarException);
    }

    private void customizeBinder() {
        binder.forField(settingRadioButton).bind(CalendarException::getSetting, CalendarException::setSetting);
        binder.forField(recurrencePattern).bind(CalendarException::getPattern, CalendarException::setPattern);
        binder.forField(endByAfter).bind(CalendarException::getEndByAfter, CalendarException::setEndByAfter);
        binder.forField(monthlyPattern).bind(CalendarException::getMonthlyPattern, CalendarException::setMonthlyPattern);
        binder.forField(numberOfDays).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getNumberOfDays, CalendarException::setNumberOfDays);
        binder.forField(numberOfWeeks).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getNumberOfWeeks, CalendarException::setNumberOfWeeks);
        binder.forField(dayOfMonth).withConverter(new ByteToDoubleConverter())
                .bind(CalendarException::getDayOfMonth, CalendarException::setDayOfMonth);
        binder.forField(numberOfMonth).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getNumberOfMonth, CalendarException::setNumberOfMonth);
        binder.forField(numberOfMonthThe).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getNumberOfMonthThe, CalendarException::setNumberOfMonthThe);
        binder.forField(numberOfOccurrence).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getNumberOfOccurrence, CalendarException::setNumberOfOccurrence);
        binder.forField(onDateDay).withConverter(new ByteToDoubleConverter())
                .bind(CalendarException::getOnDateDay, CalendarException::setOnDateDay);
        binder.bindInstanceFields(this);
    }

    private NumberField getCustomizedNumberField() {
        var numberField = new NumberField();
        numberField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        numberField.setStepButtonsVisible(true);
        numberField.setStep(1);
        numberField.setMin(1);
        return numberField;
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
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 4));
        return new VerticalLayout(horizontalLayout, formLayout);
    }

    private void customizeElements() {

        dayOfMonth.setMax(31);
        recurrencePatternContent.setSizeFull();
        start.addValueChangeListener(this::startValueChangeListener);
        onDateDay.setMax(31);
        onDateMonth.addValueChangeListener(this::onDateMonthChangeListener);
        onDateMonth.setItems(Month.values());
        numberOfWeekThe.setItems(NumberOfWeek.values());
        dayOfWeekThe.setItems(DayOfWeek.values());
        numberOfWeekYear.setItems(NumberOfWeek.values());
        dayOfWeekYear.setItems(DayOfWeek.values());
        monthYear.setItems(Month.values());

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
                item.add(numberOfOccurrence, occurrences);
            }
            else
                item.add(finish);
            return item;
        }));

        monthlyPattern.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        monthlyPattern.setItems(MonthlyPattern.values());
        monthlyPattern.addValueChangeListener(this::monthlyPatternChangeListener);
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
        yearlyPattern.addValueChangeListener(this::yearlyPatternChangeListener);
        yearlyPattern.setRenderer(new ComponentRenderer<>(yearlyPattern -> {
            var text = new Text(yearlyPattern.toString());
            var item = new HorizontalLayout(text);
            if (yearlyPattern == YearlyPattern.ON) {
                item.add(onDateDay, onDateMonth);
            }
            else {
                var of = new Text("of");
                item.add(numberOfWeekYear, dayOfWeekYear, of, monthYear);
            }
            return item;
        }));
    }

    private void onDateMonthChangeListener(AbstractField.ComponentValueChangeEvent<Select<Month>, Month> event) {
        var month = event.getValue();
        onDateDay.setMax(month.length(true));
    }

    private void startValueChangeListener(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> event) {

        var newDate = event.getValue();
        if (recurrencePattern.getValue() == RecurrencePattern.MONTHLY) {
            if (monthlyPattern.getValue() == MonthlyPattern.DAY) {
                var weekOfMonth = newDate.get(WeekFields.ISO.weekOfMonth());
                var number = weekOfMonth - 1;
                var numberOfWeek = NumberOfWeek.of((short) number);
                numberOfWeekThe.setValue(numberOfWeek);
                var dayOfWeek = newDate.getDayOfWeek();
                dayOfWeekThe.setValue(dayOfWeek);
            }
            else {
                dayOfMonth.setValue((double) newDate.getDayOfMonth());
            }
        } else if (recurrencePattern.getValue() == RecurrencePattern.YEARLY) {
            if (yearlyPattern.getValue() == YearlyPattern.ON) {
                var weekOfMonth = newDate.get(WeekFields.ISO.weekOfMonth());
                var number = weekOfMonth - 1;
                var numberOfWeek = NumberOfWeek.of((short) number);
                numberOfWeekYear.setValue(numberOfWeek);
                var dayOfWeek = newDate.getDayOfWeek();
                dayOfWeekYear.setValue(dayOfWeek);
                monthYear.setValue(newDate.getMonth());
            }
            else {
                onDateMonth.setValue(start.getValue().getMonth());
                onDateDay.setValue((double) start.getValue().getDayOfMonth());
            }
        }

        else {
            var weekOfMonth = newDate.get(WeekFields.ISO.weekOfMonth());
            var number = weekOfMonth - 1;
            var numberOfWeek = NumberOfWeek.of((short) number);
            numberOfWeekThe.setValue(numberOfWeek);
            numberOfWeekYear.setValue(numberOfWeek);
            var dayOfWeek = newDate.getDayOfWeek();
            dayOfWeekThe.setValue(dayOfWeek);
            dayOfWeekYear.setValue(dayOfWeek);
            dayOfMonth.setValue((double) newDate.getDayOfMonth());
            onDateMonth.setValue(start.getValue().getMonth());
            onDateDay.setValue((double) start.getValue().getDayOfMonth());
            monthYear.setValue(newDate.getMonth());
        }

    }

    private void yearlyPatternChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<YearlyPattern>, YearlyPattern> event) {
        var enable = event.getValue() == YearlyPattern.ON;
        onDateMonth.setEnabled(enable);
        onDateDay.setEnabled(enable);
        numberOfWeekYear.setEnabled(!enable);
        dayOfWeekYear.setEnabled(!enable);
        monthYear.setEnabled(!enable);
    }

    private void monthlyPatternChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<MonthlyPattern>, MonthlyPattern> event) {
        var enable = event.getValue() == MonthlyPattern.DAY;
        dayOfMonth.setEnabled(enable);
        numberOfMonth.setEnabled(enable);
        numberOfWeekThe.setEnabled(!enable);
        dayOfWeekThe.setEnabled(!enable);
        numberOfMonthThe.setEnabled(!enable);
    }

    private void endByAfterChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<RecurrenceEnd>, RecurrenceEnd> event) {
        numberOfOccurrence.setEnabled(event.getValue() == RecurrenceEnd.AFTER);
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

    private class Intervals extends IntervalGrid {

        Intervals() {
            setInstantiatable(this::onAddInterval);
            setDeletable(true);
        }

        public void clear() {
            grid.getListDataView().removeItems(grid.getListDataView().getItems().toList());
        }

        private Interval onAddInterval() {

            if (calendarException == null) return null;
            var newInterval = calendarException.getIntervalInstance();
            var previousTimeTo = this.grid.getListDataView().getItems().map(Interval::getTo)
                    .max(Comparator.naturalOrder()).orElse(LocalTime.MIN);
            newInterval.setFrom(previousTimeTo.plusHours(1));
            newInterval.setTo(previousTimeTo.plusHours(2));
            return newInterval;

        }

    }

    private static class IntegerToDoubleConverter implements Converter<Double, Integer> {

        @Override
        public Result<Integer> convertToModel(Double aDouble, ValueContext valueContext) {
            if (aDouble == null) return Result.ok(0);
            return Result.ok(aDouble.intValue());
        }

        @Override
        public Double convertToPresentation(Integer integer, ValueContext valueContext) {
            if (integer == null) return 0d;
            return integer.doubleValue();
        }
    }

    private static class ByteToDoubleConverter implements Converter<Double, Byte> {

        @Override
        public Result<Byte> convertToModel(Double aDouble, ValueContext valueContext) {
            if (aDouble == null) return Result.ok((byte) 0);
            return Result.ok(aDouble.byteValue());
        }

        @Override
        public Double convertToPresentation(Byte aByte, ValueContext valueContext) {
            if (aByte == null) return 0d;
            return aByte.doubleValue();
        }
    }

}
