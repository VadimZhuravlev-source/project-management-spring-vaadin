package com.pmvaadin.calendars.entity;

import com.pmvaadin.calendars.exceptiondays.ExceptionDays;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarImplTest {

    private int secondInHour = 3600;

    private LocalDateTime date20220125 = LocalDateTime.of(2022, 1, 25, 12, 23, 11);

    private LocalDateTime date20211223 = LocalDateTime.of(2021, 12, 23, 11, 56, 23);

    private LocalDate shortWorkingDay = LocalDate.of(2021, 12, 31);

    private CalendarImpl calendar = new CalendarImpl();
    private CalendarImpl calendarWithExceptions = new CalendarImpl();
    private CalendarImpl calendar12 = new CalendarImpl();
    private CalendarImpl calendar24 = new CalendarImpl();

    {
        //calendar12 = new CalendarImpl();
        calendar12.setSetting(CalendarSettings.HOURSHIFT12);
        //calendar24 = new CalendarImpl();
        calendar24.setSetting(CalendarSettings.HOURSHIFT24);

        List<ExceptionDays> exceptions = calendarWithExceptions.getCalendarException();
        exceptions.addAll(getExceptions());

    }

    private List<ExceptionDays> getExceptions() {

        List<ExceptionDays> exceptions = new ArrayList<>(7);

        // Big new year holidays
        exceptions.add(new ExceptionDays(LocalDate.of(2022, 1, 10), 0));
        exceptions.add(new ExceptionDays(LocalDate.of(2022, 1, 7), 0));
        exceptions.add(new ExceptionDays(LocalDate.of(2022, 1, 6), 0));
        exceptions.add(new ExceptionDays(LocalDate.of(2022, 1, 5), 0));
        exceptions.add(new ExceptionDays(LocalDate.of(2022, 1, 4), 0));
        exceptions.add(new ExceptionDays(LocalDate.of(2022, 1, 3), 0));
        exceptions.add(new ExceptionDays(shortWorkingDay, 7 * secondInHour));

        return exceptions;

    }

    // getDateByDuration tests

    @Test
    void getDuration_WhereDifferenceBetweenDatesIsSomeSecond() {

        LocalDateTime startDate = LocalDateTime.of(2022, 1, 25, 10, 30, 29);
        LocalDateTime finishDate = LocalDateTime.of(2022, 1, 25, 10, 30, 34);
        long calcDuration = calendarWithExceptions.getDuration(startDate, finishDate);
        assertEquals(5, calcDuration);

    }

    @Test
    void getDuration_WhereDifferenceBetweenDatesIsDay() {

        LocalDateTime startDate = LocalDateTime.of(2022, 1, 25, 10, 30, 29);
        LocalDateTime finishDate = LocalDateTime.of(2022, 1, 26, 10, 30, 34);
        long calcDuration = calendarWithExceptions.getDuration(startDate, finishDate);
        int duration = 8 * secondInHour + 5;
        assertEquals(duration, calcDuration);

    }

    @Test
    void getDuration_WhereDifferenceBetweenDatesIsPeriodWithHoliday() {

        LocalDateTime startDate = LocalDateTime.of(2021, 12, 30, 10, 30, 29);
        LocalDateTime finishDate = LocalDateTime.of(2022, 1, 11, 10, 30, 34);
        long calcDuration = calendarWithExceptions.getDuration(startDate, finishDate);
        int duration = 1 * 8 * secondInHour + 5 + 7 * secondInHour;
        assertEquals(duration, calcDuration);

    }

    @Test
    void getDuration_WhereDatesAreInPeriodWithHoliday() {

        LocalDateTime startDate = LocalDateTime.of(2022, 1, 1, 10, 30, 29);
        LocalDateTime finishDate = LocalDateTime.of(2022, 1, 9, 10, 30, 34);
        long calcDuration = calendarWithExceptions.getDuration(startDate, finishDate);
        int duration = 0;
        assertEquals(duration, calcDuration);

    }

    @Test
    void getDuration_WhereDatesStartBeforeStartTimeAndAfterFinishTime() {

        LocalDateTime startDate = LocalDateTime.of(2021, 12, 31, 7, 30, 29);
        LocalDateTime finishDate = LocalDateTime.of(2021, 12, 31, 20, 30, 34);
        long calcDuration = calendarWithExceptions.getDuration(startDate, finishDate);
        int duration = 7 * secondInHour;
        assertEquals(duration, calcDuration);

    }

    // getDateByDuration tests

    @Test
    void getDateByDuration_WhereDurationIsPlusOneDay() {

        long duration = 8L * secondInHour;
        LocalDateTime date = LocalDateTime.of(2023, 8, 29, 9, 0);
        LocalDateTime aheadDate = date.plusDays(1);
        LocalDateTime newDate = calendar.getDateByDuration(date, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsMinusOneDay() {

        long duration = - 8L * secondInHour;
        LocalDateTime date = LocalDateTime.of(2023, 8, 29, 9, 0);
        LocalDateTime aheadDate = date.minusDays(1);
        LocalDateTime newDate = calendar.getDateByDuration(date, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsPlus3Seconds() {

        long duration = 3L;
        LocalDateTime aheadDate = date20220125.plusSeconds(duration);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsMinus3Seconds() {

        long duration = 3L;
        LocalDateTime backDate = date20220125.minusSeconds(duration);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, -duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_Plus3Hours_WhereDateEndsAfterFinishTime() {

        long numberOfHours = 3L;
        long duration = numberOfHours * secondInHour;
        LocalDate day = date20220125.toLocalDate();
        LocalDateTime date = LocalDateTime.of(
                date20220125.toLocalDate(),
                LocalTime.of(20, 0)
        );
        day = day.plusDays(1);
        LocalTime newTime = calendar.getStartTime().plusHours(numberOfHours);
        LocalDateTime aheadDate = LocalDateTime.of(day, newTime);
        LocalDateTime newDate = calendar.getDateByDuration(date, duration);

        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_Plus3Hours_WhereDateStartsBeforeStartTime() {

        long numberOfHours = 3L;
        long duration = numberOfHours * secondInHour;
        LocalDate day = date20220125.toLocalDate();
        LocalDateTime date = LocalDateTime.of(
                date20220125.toLocalDate(),
                LocalTime.of(7, 0)
        );
        LocalTime newTime = calendar.getStartTime().plusHours(numberOfHours);
        LocalDateTime aheadDate = LocalDateTime.of(day, newTime);
        LocalDateTime newDate = calendar.getDateByDuration(date, duration);

        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_Minus3Hours_WhereDateEndsAfterFinishTime() {

        long numberOfHours = 3L;
        long duration = - numberOfHours * secondInHour;
        LocalDate day = date20220125.toLocalDate();
        LocalDateTime date = LocalDateTime.of(
                date20220125.toLocalDate(),
                LocalTime.of(20, 0)
        );
        LocalTime newTime = LocalTime.of(17, 0);
        newTime = newTime.minusHours(numberOfHours);
        LocalDateTime backDate = LocalDateTime.of(day, newTime);
        LocalDateTime newDate = calendar.getDateByDuration(date, duration);

        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_Minus3Hours_WhereDateStartsBeforeStartTime() {

        long numberOfHours = 3L;
        long duration = - numberOfHours * secondInHour;
        LocalDate day = date20220125.toLocalDate();
        LocalDateTime date = LocalDateTime.of(
                date20220125.toLocalDate(),
                LocalTime.of(7, 0)
        );
        day = day.minusDays(1);
        LocalTime newTime = LocalTime.of(17, 0);
        newTime = newTime.minusHours(numberOfHours);
        LocalDateTime backDate = LocalDateTime.of(day, newTime);
        LocalDateTime newDate = calendar.getDateByDuration(date, duration);

        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_Minus6Hours_WhereDateEndsOnShortDay() {

        long numberOfHours = 6L;
        long duration = - numberOfHours * secondInHour;
        LocalDateTime date = LocalDateTime.of(
                LocalDate.of(2022, 1, 11),
                LocalTime.of(11, 0)
        );

        LocalTime newTime = LocalTime.of(16, 0);
        newTime = newTime.minusHours(4);
        LocalDateTime backDate = LocalDateTime.of(shortWorkingDay, newTime);
        LocalDateTime newDate = calendarWithExceptions.getDateByDuration(date, duration);

        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsMinus3Hours() {

        long duration = 3L * secondInHour;
        LocalDateTime backDate = date20220125.minusSeconds(duration);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, -duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsPlus3WorkingDays() {

        long duration = 3L * 8 * secondInHour;
        LocalDateTime aheadDate = date20220125.plusDays(3);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsMinus3WorkingDays() {

        long duration = - 3L * 8 * secondInHour;
        LocalDateTime backDate = date20220125.minusDays(5);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsPlus3WorkingMonths() {

        long duration = 3L * 20 * 8 * secondInHour;
        LocalDateTime aheadDate = date20220125.plusDays(84);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsMinus3WorkingMonths() {

        long duration = - 3L * 20 * 8 * secondInHour;
        LocalDateTime backDate = date20220125.minusDays(84);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsPlus3Week() {

        long duration =  14L * 8L * secondInHour;
        LocalDateTime aheadDate = LocalDateTime.of(
                LocalDate.of(2022, 1, 20),
                date20211223.toLocalTime().plusHours(1)
        );
        LocalDateTime newDate = calendarWithExceptions.getDateByDuration(date20211223, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsMinus3Week() {

        long duration = - 14L * 8L * secondInHour;
        LocalDateTime backDate = LocalDateTime.of(
                LocalDate.of(2021, 12, 28),
                date20220125.toLocalTime().minusHours(1)
        );
        LocalDateTime newDate = calendarWithExceptions.getDateByDuration(date20220125, duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsMinus3DayAndBackDateIsInShortDay() {

        long duration = - 2L * 8L * secondInHour - 4L * secondInHour;
        LocalDateTime backDate = LocalDateTime.of(2021, 12, 31, 13, 0);
        LocalDateTime date = LocalDateTime.of(2022, 1, 13, 10, 0);

        LocalDateTime newDate = calendarWithExceptions.getDateByDuration(date, duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsYearForCalendar12() {

        long duration = 365L * 24L * secondInHour;
        LocalDateTime date = LocalDateTime.of(2022, 1, 13, 10, 0);
        LocalDateTime backDate = date.plusDays(2 * 365);

        LocalDateTime newDate = calendar12.getDateByDuration(date, duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsYearForCalendar24() {

        long duration = 365L * 24L * secondInHour;
        LocalDateTime date = LocalDateTime.of(2022, 1, 13, 10, 0);
        LocalDateTime backDate = date.plusDays(365);

        LocalDateTime newDate = calendar24.getDateByDuration(date, duration);
        assertEquals(backDate, newDate);

    }

}