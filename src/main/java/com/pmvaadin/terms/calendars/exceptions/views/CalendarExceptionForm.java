package com.pmvaadin.terms.calendars.exceptions.views;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.common.IntervalGrid;
import com.pmvaadin.terms.calendars.exceptions.*;
import com.pmvaadin.terms.calendars.validators.CalendarValidation;
import com.pmvaadin.terms.calendars.validators.CalendarValidationImpl;
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
    private final Select<DayOfWeek> endOfWeek = new Select<>();
    private final Checkbox everyMonday = new Checkbox("Monday", this::calculateEndByAfter);
    private final Checkbox everyTuesday = new Checkbox("Tuesday", this::calculateEndByAfter);
    private final Checkbox everyWednesday = new Checkbox("Wednesday", this::calculateEndByAfter);
    private final Checkbox everyThursday = new Checkbox("Thursday", this::calculateEndByAfter);
    private final Checkbox everyFriday = new Checkbox("Friday", this::calculateEndByAfter);
    private final Checkbox everySaturday = new Checkbox("Saturday", this::calculateEndByAfter);
    private final Checkbox everySunday = new Checkbox("Sunday", this::calculateEndByAfter);

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

    private final CalendarValidation calendarValidation = new CalendarValidationImpl();
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
        addClassName("dialog-padding-1");
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

        if (event.getValue() < field.getMin()) {
            field.setValue(field.getMin());
            return;
        }
        if (event.getValue() > field.getMax()) {
            field.setValue(field.getMax());
            return;
        }

        calculateEndByAfter(event);

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
        var start = new FormLayout();
        start.addFormItem(this.start, "Start");
        var occurrences = new Text(" occurrences");
        var endByAfterVerticalLayout = new VerticalLayout(new HorizontalLayout(numberOfOccurrence, occurrences), finish);
        var rangeOfRecurrenceLayout = new HorizontalLayout(start, endByAfter, endByAfterVerticalLayout);
        var rangeOfRecurrenceFormLayout = new VerticalLayout(new Text("Range of recurrence"), rangeOfRecurrenceLayout);
        rangeOfRecurrenceFormLayout.setSpacing(false);

        var verticalLayout = new VerticalLayout(mainLayout, intervalSetting,
                recurrencePatternFormLayout, rangeOfRecurrenceFormLayout);
        verticalLayout.setSpacing(false);

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
        FormLayout formLayout;
        if (endOfWeek.getValue() == DayOfWeek.SUNDAY)
            formLayout = new FormLayout(everyMonday, everyTuesday, everyWednesday, everyThursday, everyFriday,
                    everySaturday, everySunday);
        else
            formLayout = new FormLayout(everySunday, everyMonday, everyTuesday, everyWednesday, everyThursday,
                    everyFriday, everySaturday);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 4));

        Text endOfWeekText = new Text("The week ends on ");
        var endOfWeekLayout =  new HorizontalLayout(endOfWeekText, endOfWeek);
        return new VerticalLayout(horizontalLayout, endOfWeekLayout, formLayout);
    }

    private void customizeElements() {

        dayOfMonth.setMax(31);
        dayOfMonth.addValueChangeListener(this::calculateEndByAfter);
        recurrencePatternContent.setSizeFull();
        start.addValueChangeListener(this::startValueChangeListener);
        start.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        onDateDay.setMax(31);
        onDateDay.addValueChangeListener(this::calculateEndByAfter);
        onDateMonth.addValueChangeListener(this::onDateMonthChangeListener);
        onDateMonth.setItems(Month.values());
        onDateMonth.addThemeVariants(SelectVariant.LUMO_SMALL);
        onDateMonth.setRenderer(new ComponentRenderer<>(this::getMonthDisplayName));
        numberOfWeekThe.setItems(NumberOfWeek.values());
        numberOfWeekThe.addThemeVariants(SelectVariant.LUMO_SMALL);
        numberOfWeekThe.addValueChangeListener(this::calculateEndByAfter);
        dayOfWeekThe.setItems(DayOfWeek.values());
        dayOfWeekThe.addThemeVariants(SelectVariant.LUMO_SMALL);
        dayOfWeekThe.addValueChangeListener(this::calculateEndByAfter);
        dayOfWeekThe.setRenderer(new ComponentRenderer<>(this::getDayOfWeekDisplayName));
        numberOfWeekYear.setItems(NumberOfWeek.values());
        numberOfWeekYear.addThemeVariants(SelectVariant.LUMO_SMALL);
        numberOfWeekYear.addValueChangeListener(this::calculateEndByAfter);
        dayOfWeekYear.setItems(DayOfWeek.values());
        dayOfWeekYear.addThemeVariants(SelectVariant.LUMO_SMALL);
        dayOfWeekYear.addValueChangeListener(this::calculateEndByAfter);
        dayOfWeekYear.setRenderer(new ComponentRenderer<>(this::getDayOfWeekDisplayName));
        monthYear.setItems(Month.values());
        monthYear.addThemeVariants(SelectVariant.LUMO_SMALL);
        monthYear.addValueChangeListener(this::calculateEndByAfter);
        monthYear.setRenderer(new ComponentRenderer<>(this::getMonthDisplayName));
        intervals.setWidth("35%");
        intervals.setHeight("25%");
        finish.addValueChangeListener(this::finishValueChangeListener);
        finish.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        finish.addValueChangeListener(this::calculateEndByAfter);
        numberOfOccurrence.setMin(0);
        endOfWeek.setItems(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        endOfWeek.addThemeVariants(SelectVariant.LUMO_SMALL);
        endOfWeek.addValueChangeListener(this::endOfWeekValueChangeListener);
        endOfWeek.setRenderer(new ComponentRenderer<>(this::getDayOfWeekDisplayName));

        settingRadioButton.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        settingRadioButton.setItems(CalendarExceptionSetting.values());
        settingRadioButton.addValueChangeListener(this::settingRadioButtonChangeListener);

        recurrencePattern.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        recurrencePattern.setItems(RecurrencePattern.values());
        recurrencePattern.addValueChangeListener(this::recurrencePatternChangeListener);

        endByAfter.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        endByAfter.setItems(RecurrenceEnd.values());
        endByAfter.addValueChangeListener(this::endByAfterChangeListener);

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

    private void endOfWeekValueChangeListener(AbstractField.ComponentValueChangeEvent<Select<DayOfWeek>, DayOfWeek> event) {

        if (recurrencePattern.getValue() != RecurrencePattern.WEEKLY) return;

        var content = getWeeklyContent();

        recurrencePatternContent.removeAll();
        recurrencePatternContent.add(content);

        calculateEndByAfter(event);

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

        calculateEndByAfter(event);

    }

    private void calculateEndByAfter(AbstractField.ComponentValueChangeEvent<?, ?> event) {

        if (!refreshNumberOfOccurrence || isFirstInit) return;

        if (finish.getValue().compareTo(start.getValue()) < 0) {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setText("The end must be later than the start");
            finish.setValue(start.getValue());
        }

        if (endByAfter.getValue() == RecurrenceEnd.BY)
            refreshNumberOfOccurrence();
        else if (endByAfter.getValue() == RecurrenceEnd.AFTER)
            refreshFinish();

    }

    private void refreshNumberOfOccurrence() {

        if (recurrencePattern.getValue() == RecurrencePattern.DAILY)
            recalculateNumberOfOccurrenceForDaily();
        else if (recurrencePattern.getValue() == RecurrencePattern.WEEKLY)
            recalculateNumberOfOccurrenceForWeekly();
        else if (recurrencePattern.getValue() == RecurrencePattern.MONTHLY)
            recalculateNumberOfOccurrenceForMonthly();
        else if (recurrencePattern.getValue() == RecurrencePattern.YEARLY)
            recalculateNumberOfOccurrenceForYearly();

    }

    private void refreshFinish() {

        var newInstance = calendarException.getCalendar().getCalendarExceptionInstance();
        try {
            binder.writeBean(newInstance);
        } catch (ValidationException e) {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setText(e.getValidationErrors().toString());
            confirmDialog.open();
            return;
        }

        var map = newInstance.getExceptionAsDayConstraint();
        if (map == null) return;
        var finish = map.keySet().stream().max(Comparator.naturalOrder()).orElse(this.start.getValue());
        this.finish.setValue(finish);

    }

    private void recalculateNumberOfOccurrenceForDaily() {

        var amountOfDay = ChronoUnit.DAYS.between(start.getValue(), finish.getValue());
        var newValue = getNumberOfOccurrence((int) ++amountOfDay, everyNumberOfDays.getValue());
        refreshNumberOfOccurrence = false;
        numberOfOccurrence.setValue(newValue);
        refreshNumberOfOccurrence = true;

    }

    private void recalculateNumberOfOccurrenceForWeekly() {

        var newNumberOfOccurrence = 0;
        var amountOfDay = ChronoUnit.DAYS.between(start.getValue(), finish.getValue());
        var dayOfWeek = start.getValue().getDayOfWeek();

        var everyNumberOfWeek = everyNumberOfWeeks.getValue().intValue();
        everyNumberOfWeek--;
        var i = 0;
        while (i <= amountOfDay) {
            if (everyMonday.getValue() && dayOfWeek == DayOfWeek.MONDAY) newNumberOfOccurrence++;
            if (everyTuesday.getValue() && dayOfWeek == DayOfWeek.TUESDAY) newNumberOfOccurrence++;
            if (everyWednesday.getValue() && dayOfWeek == DayOfWeek.WEDNESDAY) newNumberOfOccurrence++;
            if (everyThursday.getValue() && dayOfWeek == DayOfWeek.THURSDAY) newNumberOfOccurrence++;
            if (everyFriday.getValue() && dayOfWeek == DayOfWeek.FRIDAY) newNumberOfOccurrence++;
            if (everySaturday.getValue() && dayOfWeek == DayOfWeek.SATURDAY) newNumberOfOccurrence++;
            if (everySunday.getValue() && dayOfWeek == DayOfWeek.SUNDAY) newNumberOfOccurrence++;

            if (dayOfWeek == endOfWeek.getValue() && everyNumberOfWeek != 0) {
                var skip = DayOfWeek.values().length * everyNumberOfWeek;
                i = i + skip;
                dayOfWeek = dayOfWeek.plus(skip);

            }

            dayOfWeek = dayOfWeek.plus(1);
            i++;

        }

        refreshNumberOfOccurrence = false;
        numberOfOccurrence.setValue((double) newNumberOfOccurrence);
        refreshNumberOfOccurrence = true;

    }

    private void recalculateNumberOfOccurrenceForMonthly() {

        var newNumberOfOccurrence = 0d;
        var startDay = start.getValue();
        var finishDay = finish.getValue();
        if (monthlyPattern.getValue() == MonthlyPattern.DAY) {
            var checkedDate = LocalDate.of(startDay.getYear(), startDay.getMonth(), dayOfMonth.getValue().intValue());
            var everyNumberOfMonth = everyNumberOfMonths.getValue().intValue();
            if (everyNumberOfMonth != 0) {
                while (checkedDate.compareTo(finishDay) <= 0) {
                    if (checkedDate.compareTo(startDay) >= 0)
                        newNumberOfOccurrence++;
                    checkedDate = checkedDate.plusMonths(everyNumberOfMonth);
                }
            }

        } else {
            var checkedDayOfMonth = LocalDate.of(startDay.getYear(), startDay.getMonth(),1);
            checkedDayOfMonth = getDayOfMonth(checkedDayOfMonth, dayOfWeekThe.getValue(), numberOfWeekThe.getValue());
            if (everyNumberOfMonthsThe.getValue().intValue() != 0) {

                var everyNumberOfMonth = everyNumberOfMonthsThe.getValue().intValue();
                var finishDate = finish.getValue();
                while (checkedDayOfMonth.compareTo(finishDate) <= 0) {
                    if (checkedDayOfMonth.compareTo(startDay) >= 0)
                        newNumberOfOccurrence++;
                    checkedDayOfMonth = checkedDayOfMonth.plusMonths(everyNumberOfMonth);
                    checkedDayOfMonth = getDayOfMonth(checkedDayOfMonth, dayOfWeekThe.getValue(), numberOfWeekThe.getValue());
                }
            }

        }

        refreshNumberOfOccurrence = false;
        numberOfOccurrence.setValue(newNumberOfOccurrence);
        refreshNumberOfOccurrence = true;

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

            while (checkedDate.compareTo(finish.getValue()) <= 0) {

                if (checkedDate.compareTo(startDate) >= 0
                        && !(!checkedDate.isLeapYear() && day == daysOfFebruaryOfLeapYear && month == Month.FEBRUARY)) {
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
            var checkedDate = getDayOfMonth(iteratedDate, dayOfWeek, numberOfWeek);

            while (checkedDate.compareTo(finish.getValue()) <= 0) {
                if (checkedDate.compareTo(startDate) >= 0)
                    newNumberOfOccurrence++;
                iteratedDate = iteratedDate.plusYears(1);
                checkedDate = getDayOfMonth(iteratedDate, dayOfWeek, numberOfWeek);
            }

        }

        refreshNumberOfOccurrence = false;
        numberOfOccurrence.setValue(newNumberOfOccurrence);
        refreshNumberOfOccurrence = true;

    }

    private double getNumberOfOccurrence(int number, double denominator) {
        if (denominator == 0) return 0;
        var newValueDouble = (number / denominator);
        var newValueInt = (int) newValueDouble;
        if (newValueInt < newValueDouble) newValueInt++;
        return newValueInt;
    }

    private LocalDate getDayOfMonth(LocalDate initDate, DayOfWeek dayOfWeek, NumberOfWeek numberOfWeek) {
        return CalendarException.getDayOfMonth(initDate, dayOfWeek, numberOfWeek);
    }

    private void onDateMonthChangeListener(AbstractField.ComponentValueChangeEvent<Select<Month>, Month> event) {
        var month = event.getValue();
        var daysOfMonth = month.length(true);
        if (onDateDay.getValue().intValue() > daysOfMonth)
            onDateDay.setValue((double) daysOfMonth);
        onDateDay.setMax(daysOfMonth);
        calculateEndByAfter(event);
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
        calculateEndByAfter(event);

    }

    private void yearlyPatternChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<YearlyPattern>, YearlyPattern> event) {
        var enable = event.getValue() == YearlyPattern.ON;
        onDateMonth.setEnabled(enable);
        onDateDay.setEnabled(enable);
        numberOfWeekYear.setEnabled(!enable);
        dayOfWeekYear.setEnabled(!enable);
        monthYear.setEnabled(!enable);
        calculateEndByAfter(event);
    }

    private void monthlyPatternChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<MonthlyPattern>, MonthlyPattern> event) {
        var enable = event.getValue() == MonthlyPattern.DAY;
        dayOfMonth.setEnabled(enable);
        everyNumberOfMonths.setEnabled(enable);
        numberOfWeekThe.setEnabled(!enable);
        dayOfWeekThe.setEnabled(!enable);
        everyNumberOfMonthsThe.setEnabled(!enable);
        calculateEndByAfter(event);
    }

    private void endByAfterChangeListener(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<RecurrenceEnd>, RecurrenceEnd> event) {
        numberOfOccurrence.setEnabled(event.getValue() == RecurrenceEnd.AFTER);
        finish.setEnabled(event.getValue() == RecurrenceEnd.BY);
        calculateEndByAfter(event);
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
        calculateEndByAfter(event);

    }

    private void customizeHeader() {

        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> {
                    fireEvent(new CloseEvent(this, this.calendarException));
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
            fireEvent(new SaveEvent(this, this.calendarException));
            close();

        });

        Button close = new Button("Cancel");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(event ->
        {
            fireEvent(new CloseEvent(this, this.calendarException));
            close();
        });

        getFooter().add(ok, close);

    }

    private boolean validate() {
        if (numberOfOccurrence.getValue() == 0) throw
                new StandardError("The recurrence as presently configured will not have any occurrences.");
        calendarValidation.validateIntervals(intervals.getItems());
        return true;
    }

    private void writeChanges() throws ValidationException {
        binder.writeBean(this.calendarException);
        this.calendarException.setIntervals(intervals.getItems());
        var map = this.calendarException.getExceptionAsDayConstraint();
        var finish = map.keySet().stream().max(Comparator.naturalOrder()).orElse(this.calendarException.getStart());
        if (finish == null) return;
        this.calendarException.setEndByAfter(RecurrenceEnd.AFTER);
        this.calendarException.setFinish(finish);
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
            grid.setHeight("200px");
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
