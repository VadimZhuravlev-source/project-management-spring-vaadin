package com.pmvaadin.terms.calendars.view;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import java.time.DayOfWeek;
import java.time.LocalDate;

@CssImport(value = "./styles/test_year-month-calendar.css", themeFor = "vaadin-month-calendar")
public class YearCalendar extends VerticalLayout {

    private CalendarForm calendarForm;
    private final IntegerField yearChanger = new IntegerField();
    private final com.flowingcode.addons.ycalendar.YearCalendar yearCalendar = new com.flowingcode.addons.ycalendar.YearCalendar();

    public YearCalendar(CalendarForm calendarForm) {
        this.calendarForm = calendarForm;
        addClassName("year-demo");

        yearChanger.setStepButtonsVisible(true);
        yearChanger.setStep(1);
        yearChanger.setMin(1);

        yearCalendar.setClassNameGenerator(this::classNameGenerator);

        yearChanger.setValue(yearCalendar.getYear());
        yearChanger.addValueChangeListener(e -> yearCalendar.setYear(e.getValue()));

        add(yearChanger, yearCalendar);

    }

    public IntegerField getYearChanger() {
        return yearChanger;
    }

    private String classNameGenerator(LocalDate date) {

        if (isException(date)) {
            return "exception";
        }

        if (isWorkingWeek(date)) {
            return "workingWeek";
        }

        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return "weekend";
        }
        return null;

    }

    private boolean isException(LocalDate date) {
        return this.calendarForm.getExceptionsDate().containsKey(date);
    }

    private boolean isWorkingWeek(LocalDate date) {
        return this.calendarForm.getExceptionsDate().containsKey(date);
    }

}
