package com.pmvaadin.terms.calendars.frontend.view;

import com.flowingcode.addons.ycalendar.MonthCalendar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

@CssImport(value = "./styles/year-month-calendar-colors.css", themeFor = "vaadin-month-calendar")
public class YearCalendar extends VerticalLayout {

    private final CalendarForm calendarForm;
    private final com.flowingcode.addons.ycalendar.YearCalendar yearCalendar = new com.flowingcode.addons.ycalendar.YearCalendar();
    private final TextField month = new TextField();
    private final MonthCalendar monthCalendar = new MonthCalendar(YearMonth.now());
    public YearCalendar(CalendarForm calendarForm) {
        this.calendarForm = calendarForm;
        addClassName("year-demo");

        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setItems("Year", "Month");
        radioGroup.setValue("Month");

        month.setValue(getDisplayedName(monthCalendar.getYearMonth()));
        month.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        month.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        var doubleLeft = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
        doubleLeft.addThemeVariants(ButtonVariant.LUMO_SMALL);
        doubleLeft.addClickListener(event -> {
            monthCalendar.setYearMonth(monthCalendar.getYearMonth().minusYears(1));
            month.setValue(getDisplayedName(monthCalendar.getYearMonth()));
        });
        var doubleRight = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));
        doubleRight.addThemeVariants(ButtonVariant.LUMO_SMALL);
        doubleRight.addClickListener(event -> {
            monthCalendar.setYearMonth(monthCalendar.getYearMonth().plusYears(1));
            month.setValue(getDisplayedName(monthCalendar.getYearMonth()));
        });
        var left = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        left.addThemeVariants(ButtonVariant.LUMO_SMALL);
        left.addClickListener(event -> {
            monthCalendar.setYearMonth(monthCalendar.getYearMonth().minusMonths(1));
            month.setValue(getDisplayedName(monthCalendar.getYearMonth()));
        });
        var right = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        right.addThemeVariants(ButtonVariant.LUMO_SMALL);
        right.addClickListener(event -> {
            monthCalendar.setYearMonth(monthCalendar.getYearMonth().plusMonths(1));
            month.setValue(getDisplayedName(monthCalendar.getYearMonth()));
        });

        var horLayout = new HorizontalLayout(doubleLeft, left, month, right, doubleRight);
        month.setValue(getDisplayedName(YearMonth.now()));
        monthCalendar.setClassNameGenerator(this::classNameGenerator);
        monthCalendar.setSizeFull();
        var monthContent = new VerticalLayout(horLayout, monthCalendar);
        monthContent.setAlignItems(Alignment.CENTER);

        var yearChanger = new IntegerField();
        yearChanger.setStepButtonsVisible(true);
        yearChanger.setStep(1);
        yearChanger.setMin(1);

        yearCalendar.setClassNameGenerator(this::classNameGenerator);
        yearChanger.setValue(yearCalendar.getYear());
        yearChanger.addValueChangeListener(e -> yearCalendar.setYear(e.getValue()));
        var yearContent = new VerticalLayout(yearChanger, yearCalendar);

        var content = new HorizontalLayout(monthContent, yearContent);
        monthContent.setVisible(true);
        yearContent.setVisible(false);
        radioGroup.addValueChangeListener(event -> {
            if (event.getValue().equals("Year")) {
                monthContent.setVisible(false);
                yearContent.setVisible(true);
            } else {
                monthContent.setVisible(true);
                yearContent.setVisible(false);
            }
        });

        add(radioGroup, content);

    }

    private String getDisplayedName(YearMonth yearMonth) {
        return yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear();
    }

    public void refreshAll() {
        yearCalendar.refreshAll();
        monthCalendar.refreshAll();
    }

    private String classNameGenerator(LocalDate date) {

        if (isException(date)) {
            return "exception";
        }

        if (isWorkingWeek(date)) {
            return "workingWeek";
        }

        var weekends = this.calendarForm.getWeekends();
        if (weekends.contains(date.getDayOfWeek())) {
            return "weekend";
        }
        return null;

    }

    private boolean isException(LocalDate date) {
        return this.calendarForm.getExceptionsDate().containsKey(date);
    }

    private boolean isWorkingWeek(LocalDate date) {
        return this.calendarForm.getWorkingWeeks()
                .anyMatch(workingWeek -> !workingWeek.isDefault()
                        && date.compareTo(workingWeek.getStart()) >= 0 && date.compareTo(workingWeek.getFinish()) <= 0);
    }



}
