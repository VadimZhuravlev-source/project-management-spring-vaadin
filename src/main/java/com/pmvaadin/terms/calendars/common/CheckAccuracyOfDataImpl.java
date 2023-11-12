package com.pmvaadin.terms.calendars.common;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarSettings;
import com.pmvaadin.terms.calendars.exceptiondays.ExceptionDay;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class CheckAccuracyOfDataImpl implements CheckAccuracyOfData {

    private int availableNumberOfSeconds;
    private Calendar calendar;

    @Override
    public boolean check(Calendar calendar) {

        this.calendar = calendar;
        var startTime = calendar.getStartTime();
        this.availableNumberOfSeconds = Calendar.NUMBER_OF_SECONDS_IN_AN_HOUR * 24 - startTime.getSecond();

        checkDayOfWeek();
        checkExceptions();

        return true;

    }

    private void checkDayOfWeek() {

//        if (this.calendar.getSetting() != CalendarSettings.DAYSOFWEEKSETTINGS) return;
//
//        var daysOfWeek = this.calendar.getDaysOfWeekSettings();
//
//        var messageZeroDuration = new StandardError("The sum of working hours cannot be less than or equal to zero");
//        if (daysOfWeek == null) throw messageZeroDuration;
//
//        var sum = 0;
//        for (DayOfWeekSettings dayOfWeekSettings : daysOfWeek) {
//            sum = sum + dayOfWeekSettings.getCountHours();
//            if (dayOfWeekSettings.getCountHours() <= this.availableNumberOfSeconds) continue;
//            var dayOfWeek = DayOfWeek.of(dayOfWeekSettings.getDayOfWeek());
//            var text = getTextMessageExceedingWorkingDay();
//            text = text.replace(":day", dayOfWeek.toString());
//            throw new StandardError(text);
//        }
//
//        if (sum == 0) throw messageZeroDuration;

    }

    private void checkExceptions() {

        var calendarExceptions = this.calendar.getCalendarException();
        if (calendarExceptions == null) return;

        Set<LocalDate> days = new HashSet<>(calendarExceptions.size());
        for (ExceptionDay exceptionDay: calendarExceptions) {

            var day = exceptionDay.getDate();
            if (days.contains(day)) throw new StandardError("");
            days.add(day);

            if (exceptionDay.getDuration() <= this.availableNumberOfSeconds) continue;
            var text = getTextMessageExceedingWorkingDay();
            text = text.replace(":day", day.toString());
            throw new StandardError(text);

        }

    }

    private String getTextMessageExceedingWorkingDay() {
        return
                """
                The number of hours in the :day cannot exceed the number of hours from the start of the working day to the end of the working day.
                """;
    }

}
