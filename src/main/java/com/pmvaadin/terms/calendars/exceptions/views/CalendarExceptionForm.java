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
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
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
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.Locale;

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

    private final NumberField everyNumberOfDays = getCustomizedNumberField();
    private final NumberField everyNumberOfWeeks = getCustomizedNumberField();
    private final Checkbox everyMonday = new Checkbox("Monday", this::calculateNumberOfOccurrence);
    private final Checkbox everyTuesday = new Checkbox("Tuesday", this::calculateNumberOfOccurrence);
    private final Checkbox everyWednesday = new Checkbox("Wednesday", this::calculateNumberOfOccurrence);
    private final Checkbox everyThursday = new Checkbox("Thursday", this::calculateNumberOfOccurrence);
    private final Checkbox everyFriday = new Checkbox("Friday", this::calculateNumberOfOccurrence);
    private final Checkbox everySaturday = new Checkbox("Saturday", this::calculateNumberOfOccurrence);
    private final Checkbox everySunday = new Checkbox("Sunday", this::calculateNumberOfOccurrence);

    private final RadioButtonGroup<MonthlyPattern> monthlyPattern = new RadioButtonGroup<>();

    private final NumberField dayOfMonth = getCustomizedNumberField();
    private final NumberField everyNumberOfMonths = getCustomizedNumberField();
    private final Select<NumberOfWeek> numberOfWeekThe = new Select<>();
    private final Select<DayOfWeek> dayOfWeekThe = new Select<>();
    private final NumberField everyNumberOfMonthsThe = getCustomizedNumberField();

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
    private boolean isFirstInit = true;
    private boolean refreshNumberOfOccurrence = false;

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
        isFirstInit = false;
    }

    private void customizeBinder() {
        binder.forField(settingRadioButton).bind(CalendarException::getSetting, CalendarException::setSetting);
        binder.forField(recurrencePattern).bind(CalendarException::getPattern, CalendarException::setPattern);
        binder.forField(endByAfter).bind(CalendarException::getEndByAfter, CalendarException::setEndByAfter);
        binder.forField(monthlyPattern).bind(CalendarException::getMonthlyPattern, CalendarException::setMonthlyPattern);
        binder.forField(everyNumberOfDays).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getEveryNumberOfDays, CalendarException::setEveryNumberOfDays);
        binder.forField(everyNumberOfWeeks).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getEveryNumberOfWeeks, CalendarException::setEveryNumberOfWeeks);
        binder.forField(dayOfMonth).withConverter(new ByteToDoubleConverter())
                .bind(CalendarException::getDayOfMonth, CalendarException::setDayOfMonth);
        binder.forField(everyNumberOfMonths).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getEveryNumberOfMonths, CalendarException::setEveryNumberOfMonths);
        binder.forField(everyNumberOfMonthsThe).withConverter(new IntegerToDoubleConverter())
                .bind(CalendarException::getEveryNumberOfMonthsThe, CalendarException::setEveryNumberOfMonthsThe);
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
        numberField.addValueChangeListener(event -> numberFieldValueChangeListener(event, numberField));
        return numberField;
    }

    private void numberFieldValueChangeListener(AbstractField.ComponentValueChangeEvent<NumberField, Double> event, NumberField field) {

        if (event.getValue() == null) {
            field.setValue(event.getOldValue());
            return;
        }

        if (event.getValue() <= field.getMin()) {
            field.setValue(field.getMin());
            return;
        }
        if (event.getValue() >= field.getMax()) {
            field.setValue(field.getMax());
            return;
        }

        if (field == numberOfOccurrence)
            return;
        calculateNumberOfOccurrence(event);

    }

    private void customizeForm() {

        setWidth("55%");
        setDraggable(true);
        setResizable(true);
        addClassName("calendar_exception-form");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setModal(false);
        setCloseOnEsc(false);

        var mainLayout = new FormLayout();
        mainLayout.addFormItem(name, "Name");

        var textWorkingTimes = new Text("Set working times for this exceptions");
        var intervalSetting = new VerticalLayout(textWorkingTimes, settingRadioButton, intervals);
        intervalSetting.setWidthFull();
        intervalSetting.setSpacing(false);

        var recurrencePatternLayout = new HorizontalLayout(recurrencePattern, recurrencePatternContent);
        recurrencePatternLayout.setWidthFull();
        recurrencePatternLayout.setSpacing(false);
        var recurrencePatternFormLayout = new VerticalLayout(new Text("Recurrence pattern"), recurrencePatternLayout);
        recurrencePatternFormLayout.setSpacing(false);

        // range of occurrences fields
        var rangeOfRecurrenceLayout = new HorizontalLayout();
        var start = new FormLayout();
        start.addFormItem(this.start, "Start");
        rangeOfRecurrenceLayout.add(start);

        rangeOfRecurrenceLayout.add(endByAfter);

        var rangeOfRecurrenceFormLayout = new VerticalLayout(new Text("Range of recurrence"), rangeOfRecurrenceLayout);
        rangeOfRecurrenceFormLayout.setSpacing(false);
        var verticalLayout = new VerticalLayout(mainLayout, intervalSetting,
                recurrencePatternFormLayout, rangeOfRecurrenceFormLayout);
        verticalLayout.setWidthFull();

        add(verticalLayout);

    }

    private Component getDailyContent() {
        Text every = new Text("Every");
        Text days = new Text("days");
        return new HorizontalLayout(every, everyNumberOfDays, days);
    }

    private Component getWeeklyContent() {
        Text every = new Text("Recur every");
        Text weeks = new Text(" week(s) on:");
        var horizontalLayout =  new HorizontalLayout(every, everyNumberOfWeeks, weeks);
        var formLayout = new FormLayout(everyMonday, everyTuesday, everyWednesday, everyThursday, everyFriday,
                everySaturday, everySunday);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 4));
        return new VerticalLayout(horizontalLayout, formLayout);
    }

    private void customizeElements() {

        dayOfMonth.setMax(31);
        dayOfMonth.addValueChangeListener(this::calculateNumberOfOccurrence);
        recurrencePatternContent.setSizeFull();
        start.addValueChangeListener(this::startValueChangeListener);
        start.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        onDateDay.setMax(31);
        onDateDay.addValueChangeListener(this::calculateNumberOfOccurrence);
        onDateMonth.addValueChangeListener(this::onDateMonthChangeListener);
        onDateMonth.setItems(Month.values());
        onDateMonth.addThemeVariants(SelectVariant.LUMO_SMALL);
        onDateMonth.setRenderer(new ComponentRenderer<>(this::getMonthDisplayName));
        numberOfWeekThe.setItems(NumberOfWeek.values());
        numberOfWeekThe.addThemeVariants(SelectVariant.LUMO_SMALL);
        numberOfWeekThe.addValueChangeListener(this::calculateNumberOfOccurrence);
        dayOfWeekThe.setItems(DayOfWeek.values());
        dayOfWeekThe.addThemeVariants(SelectVariant.LUMO_SMALL);
        dayOfWeekThe.addValueChangeListener(this::calculateNumberOfOccurrence);
        dayOfWeekThe.setRenderer(new ComponentRenderer<>(this::getDayOfWeekDisplayName));
        numberOfWeekYear.setItems(NumberOfWeek.values());
        numberOfWeekYear.addThemeVariants(SelectVariant.LUMO_SMALL);
        numberOfWeekYear.addValueChangeListener(this::calculateNumberOfOccurrence);
        dayOfWeekYear.setItems(DayOfWeek.values());
        dayOfWeekYear.addThemeVariants(SelectVariant.LUMO_SMALL);
        dayOfWeekYear.addValueChangeListener(this::calculateNumberOfOccurrence);
        dayOfWeekYear.setRenderer(new ComponentRenderer<>(this::getDayOfWeekDisplayName));
        monthYear.setItems(Month.values());
        monthYear.addThemeVariants(SelectVariant.LUMO_SMALL);
        monthYear.addValueChangeListener(this::calculateNumberOfOccurrence);
        monthYear.setRenderer(new ComponentRenderer<>(this::getMonthDisplayName));
        intervals.setWidth("35%");
        intervals.setHeight("25%");
        finish.addValueChangeListener(this::finishValueChangeListener);
        finish.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        finish.addValueChangeListener(this::calculateNumberOfOccurrence);

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
                item.add(dayOfMonth, ofEvery, everyNumberOfMonths, months);
            }
            else {
                var months = new Text("months");
                item.add(numberOfWeekThe, dayOfWeekThe, ofEvery, everyNumberOfMonthsThe, months);
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

    private Component getMonthDisplayName(Month month) {
        return new Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault()));
    }

    private Component getDayOfWeekDisplayName(DayOfWeek dayOfWeek) {
        return new Text(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()));
    }

    private void finishValueChangeListener(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> event) {
        var selectedDate = event.getValue();
        if (selectedDate == null) return;
        if (selectedDate.compareTo(start.getValue()) < 0) {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setText("The end must be later than the start");
            finish.setValue(event.getOldValue());
            return;
        }

        calculateNumberOfOccurrence(event);

    }

    private void calculateNumberOfOccurrence(AbstractField.ComponentValueChangeEvent<?, ?> event) {

        if (!refreshNumberOfOccurrence || isFirstInit) return;

        if (endByAfter.getValue() == RecurrenceEnd.AFTER) return;

        if (finish.getValue().compareTo(start.getValue()) < 0) {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setText("The end must be later than the start");
            finish.setValue(start.getValue());
        }

        if (recurrencePattern.getValue() == RecurrencePattern.DAILY)
            recalculateNumberOfOccurrenceForDaily();
        else if (recurrencePattern.getValue() == RecurrencePattern.WEEKLY)
            recalculateNumberOfOccurrenceForWeekly();
        else if (recurrencePattern.getValue() == RecurrencePattern.MONTHLY)
            recalculateNumberOfOccurrenceForMonthly();
        else if (recurrencePattern.getValue() == RecurrencePattern.YEARLY)
            recalculateNumberOfOccurrenceForYearly();

    }

    private void recalculateNumberOfOccurrenceForDaily() {

        var amountOfDay = ChronoUnit.DAYS.between(start.getValue(), finish.getValue());
        var newValue = getNumberOfOccurrence((int) ++amountOfDay, everyNumberOfDays.getValue());
        numberOfOccurrence.setValue(newValue);

    }

    private void recalculateNumberOfOccurrenceForWeekly() {

        var newNumberOfOccurrence = 0;
        var amountOfDay = ChronoUnit.DAYS.between(start.getValue(), finish.getValue());
        amountOfDay++;
        var dayOfWeek = start.getValue().getDayOfWeek();
        for (int i = 0; i <= amountOfDay; i++) {
            if (everyMonday.getValue() && dayOfWeek == DayOfWeek.MONDAY) newNumberOfOccurrence++;
            if (everyTuesday.getValue() && dayOfWeek == DayOfWeek.TUESDAY) newNumberOfOccurrence++;
            if (everyWednesday.getValue() && dayOfWeek == DayOfWeek.WEDNESDAY) newNumberOfOccurrence++;
            if (everyThursday.getValue() && dayOfWeek == DayOfWeek.THURSDAY) newNumberOfOccurrence++;
            if (everyFriday.getValue() && dayOfWeek == DayOfWeek.FRIDAY) newNumberOfOccurrence++;
            if (everySaturday.getValue() && dayOfWeek == DayOfWeek.SATURDAY) newNumberOfOccurrence++;
            if (everySunday.getValue() && dayOfWeek == DayOfWeek.SUNDAY) newNumberOfOccurrence++;
            dayOfWeek = dayOfWeek.plus(1);
        }
        //newNumberOfOccurrence = (int) (newNumberOfOccurrence / everyNumberOfWeeks.getValue());

//        var denominator = everyNumberOfWeeks.getValue();
//        if (denominator == 0) return;
//        var newValueDouble = (newNumberOfOccurrence / denominator);
//        var newValueInt = (int) newValueDouble;
//        if (newValueInt < newValueDouble) newValueInt++;
        var newValue = getNumberOfOccurrence(newNumberOfOccurrence, everyNumberOfWeeks.getValue());
        numberOfOccurrence.setValue(newValue);

        //numberOfOccurrence.setValue((double) newNumberOfOccurrence);

    }

    private void recalculateNumberOfOccurrenceForMonthly() {

        var newNumberOfOccurrence = 0d;
        var startDay = start.getValue();
        if (monthlyPattern.getValue() == MonthlyPattern.DAY) {
            var amountOfMonth = ChronoUnit.MONTHS.between(startDay, finish.getValue());
            //newNumberOfOccurrence = (int) (amountOfMonth / everyNumberOfMonths.getValue());
            newNumberOfOccurrence = getNumberOfOccurrence((int) ++amountOfMonth, everyNumberOfMonths.getValue());
            //if (startDay.getDayOfMonth() <= dayOfMonth.getValue()) newNumberOfOccurrence++;
        } else {
            var starOfMonth = LocalDate.of(startDay.getYear(), startDay.getMonth(),1);
            starOfMonth = getDateOfMonth(starOfMonth, dayOfWeekThe.getValue(), numberOfWeekThe.getValue());
            var numberOfSuccessiveWeek = NumberOfWeek.FIRST.getCode();
            var numberOfDays = 0;
            var previousMonth = starOfMonth.getMonth();
            while (starOfMonth.compareTo(startDay) >= 0 && starOfMonth.compareTo(finish.getValue()) <= 0) {
                if (numberOfSuccessiveWeek.equals(numberOfWeekThe.getValue().getCode())) {
                    numberOfDays++;
                }
                starOfMonth = starOfMonth.plusWeeks(1);
                numberOfSuccessiveWeek++;
                if (starOfMonth.getMonth() != previousMonth) {
                    if (numberOfWeekThe.getValue() == NumberOfWeek.LAST && numberOfSuccessiveWeek.equals(NumberOfWeek.FOURTH.getCode())) {
                        numberOfDays++;
                    }
                    numberOfSuccessiveWeek = NumberOfWeek.FIRST.getCode();
                    previousMonth = starOfMonth.getMonth();

                }
            }

//            newNumberOfOccurrence = (int) (numberOfDays / everyNumberOfMonthsThe.getValue());
//            if (numberOfDays != 0 && newNumberOfOccurrence == 0) newNumberOfOccurrence = 1;
            newNumberOfOccurrence = getNumberOfOccurrence(numberOfDays, everyNumberOfMonths.getValue());
        }

        numberOfOccurrence.setValue(newNumberOfOccurrence);

    }

    private void recalculateNumberOfOccurrenceForYearly() {

        var newNumberOfOccurrence = 0d;

        var startDate = start.getValue();
        if (yearlyPattern.getValue() == YearlyPattern.ON) {

            var day = onDateDay.getValue().intValue();
            var month = onDateMonth.getValue();
            var daysOfFebruaryOfLeapYear = 29;

            var checkedDate = startDate;
            if (!checkedDate.isLeapYear() && day == daysOfFebruaryOfLeapYear && month == Month.FEBRUARY) {
                while (!checkedDate.isLeapYear())
                    checkedDate = checkedDate.plusYears(1);
            }
            checkedDate = LocalDate.of(checkedDate.getYear(), month, day);

            while (checkedDate.compareTo(startDate) >= 0
                    && checkedDate.compareTo(finish.getValue()) <= 0) {

                if (checkedDate.isLeapYear() || day != daysOfFebruaryOfLeapYear || month != Month.FEBRUARY) {
                    newNumberOfOccurrence++;
                }

                checkedDate = checkedDate.plusYears(1);
                if (checkedDate.isLeapYear() && day == daysOfFebruaryOfLeapYear && month == Month.FEBRUARY)
                    checkedDate = LocalDate.of(checkedDate.getYear(), month, day);

            }

        } else if (yearlyPattern.getValue() == YearlyPattern.THE) {

            var numberOfWeek = numberOfWeekYear.getValue();
            var dayOfWeek = dayOfWeekYear.getValue();
            var monthOfYear = monthYear.getValue();

            var iteratedDate = LocalDate.of(startDate.getYear(), monthOfYear, 1);
            var checkedDate = getDateOfMonth(iteratedDate, dayOfWeek, numberOfWeek);

            while (checkedDate.compareTo(startDate) >= 0 && checkedDate.compareTo(finish.getValue()) <= 0) {
                newNumberOfOccurrence++;
                iteratedDate = iteratedDate.plusYears(1);
                checkedDate = getDateOfMonth(iteratedDate, dayOfWeek, numberOfWeek);
            }

        }

        numberOfOccurrence.setValue(newNumberOfOccurrence);

    }

    private double getNumberOfOccurrence(int number, double denominator) {
        if (denominator == 0) return 0;
        var newValueDouble = (number / denominator);
        var newValueInt = (int) newValueDouble;
        if (newValueInt < newValueDouble) newValueInt++;
        return newValueInt;
    }

    private LocalDate getDateOfMonth(LocalDate initDate, DayOfWeek dayOfWeek, NumberOfWeek numberOfWeek) {

        var monthOfYear = initDate.getMonth();
        var checkedDate = LocalDate.of(initDate.getYear(), monthOfYear, 1);
        while (dayOfWeek != checkedDate.getDayOfWeek())
            checkedDate = checkedDate.plusDays(1);
        var currentNumberOfWeek = NumberOfWeek.FIRST.getCode();
        while (checkedDate.getMonth() == monthOfYear
                && !currentNumberOfWeek.equals(numberOfWeek.getCode())) {
            currentNumberOfWeek++;
            checkedDate = checkedDate.plusWeeks(1);
        }
        if (checkedDate.getMonth() != monthOfYear) {
            checkedDate = checkedDate.minusWeeks(1);
        }
        return checkedDate;
    }

    private void onDateMonthChangeListener(AbstractField.ComponentValueChangeEvent<Select<Month>, Month> event) {
        var month = event.getValue();
        var daysOfMonth = month.length(true);
        if (onDateDay.getValue().intValue() > daysOfMonth)
            onDateDay.setValue((double) daysOfMonth);
        onDateDay.setMax(daysOfMonth);
        calculateNumberOfOccurrence(event);
    }

    private void startValueChangeListener(AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate> event) {

        refreshNumberOfOccurrence = false;
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

        refreshNumberOfOccurrence = true;
        calculateNumberOfOccurrence(event);

    }

    private void yearlyPatternChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<YearlyPattern>, YearlyPattern> event) {
        var enable = event.getValue() == YearlyPattern.ON;
        onDateMonth.setEnabled(enable);
        onDateDay.setEnabled(enable);
        numberOfWeekYear.setEnabled(!enable);
        dayOfWeekYear.setEnabled(!enable);
        monthYear.setEnabled(!enable);
        calculateNumberOfOccurrence(event);
    }

    private void monthlyPatternChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<MonthlyPattern>, MonthlyPattern> event) {
        var enable = event.getValue() == MonthlyPattern.DAY;
        dayOfMonth.setEnabled(enable);
        everyNumberOfMonths.setEnabled(enable);
        numberOfWeekThe.setEnabled(!enable);
        dayOfWeekThe.setEnabled(!enable);
        everyNumberOfMonthsThe.setEnabled(!enable);
        calculateNumberOfOccurrence(event);
    }

    private void endByAfterChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<RecurrenceEnd>, RecurrenceEnd> event) {
        numberOfOccurrence.setEnabled(event.getValue() == RecurrenceEnd.AFTER);
        finish.setEnabled(event.getValue() == RecurrenceEnd.BY);
        calculateNumberOfOccurrence(event);
    }

    private void settingRadioButtonChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<CalendarExceptionSetting>, CalendarExceptionSetting> event) {
        intervals.setEnabled(event.getValue() == CalendarExceptionSetting.WORKING_TIMES);
        if (event.getValue() == CalendarExceptionSetting.NONWORKING) intervals.clear();
        else if (!isFirstInit) intervals.setItems(this.calendarException.getDefaultIntervals());
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
        calculateNumberOfOccurrence(event);

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
        if (numberOfOccurrence.getValue() == 0) throw
                new StandardError("The recurrence as presently configured will not have any occurrences.");
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
            grid.addThemeVariants(GridVariant.LUMO_COMPACT);
            this.addToolbarSmallThemeVariant();
            //grid.setSizeFull();
        }

        public void clear() {
            grid.getListDataView().removeItems(grid.getListDataView().getItems().toList());
        }

        private Interval onAddInterval() {

            if (calendarException == null) return null;
            var newInterval = calendarException.getIntervalInstance();
            var previousTimeTo = this.grid.getListDataView().getItems().map(Interval::getTo)
                    .max(Comparator.naturalOrder()).orElse(LocalTime.MIN);
            //TODO interval validation
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
