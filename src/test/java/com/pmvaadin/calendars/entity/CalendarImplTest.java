package com.pmvaadin.calendars.entity;

import com.pmvaadin.calendars.exceptiondays.ExceptionDays;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarImplTest {

    private int secondInHour = 3600;

    private LocalDateTime date20220125 = LocalDateTime.of(2022, 1, 25, 12, 23, 11);

    private LocalDateTime date20211223 = LocalDateTime.of(2021, 12, 23, 11, 56, 23);


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
        exceptions.add(new ExceptionDays(LocalDate.of(2021, 12, 31), 7 * secondInHour));

        return exceptions;

    }

    @Test
    void getDuration() {


    }

    @Test
    void getDateByDuration_WhereDurationPlus3Seconds() {

        long duration = 3;
        LocalDateTime aheadDate = date20220125.plusSeconds(duration);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationMinus3Seconds() {

        long duration = 3;
        LocalDateTime backDate = date20220125.minusSeconds(duration);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, -duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationPlus3Hours() {

        long duration = 3 * secondInHour;
        LocalDateTime aheadDate = date20220125.plusSeconds(duration);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationMinus3Hours() {

        long duration = 3 * secondInHour;
        LocalDateTime backDate = date20220125.minusSeconds(duration);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, -duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationPlus3WorkingDays() {

        long duration = 3 * 8 * secondInHour;
        LocalDateTime aheadDate = date20220125.plusDays(3);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationMinus3WorkingDays() {

        long duration = 3 * 8 * secondInHour;
        LocalDateTime backDate = date20220125.minusDays(5);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, -duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationPlus3WorkingMonths() {

        long duration = 3 * 20 * 8 * secondInHour;
        LocalDateTime aheadDate = date20220125.plusDays(84);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationMinus3WorkingMonths() {

        long duration = 3 * 20 * 8 * secondInHour;
        LocalDateTime backDate = date20220125.minusDays(84);
        LocalDateTime newDate = calendar.getDateByDuration(date20220125, -duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationPlus3Week() {

        long duration =  14 * 8 * secondInHour;
        LocalDateTime aheadDate = LocalDateTime.of(
                LocalDate.of(2022, 1, 20),
                date20211223.toLocalTime().plusHours(1)
        );
        LocalDateTime newDate = calendarWithExceptions.getDateByDuration(date20211223, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationMinus3Week() {

        long duration =  14 * 8 * secondInHour;
        LocalDateTime backDate = LocalDateTime.of(
                LocalDate.of(2021, 12, 28),
                date20220125.toLocalTime().minusHours(1)
        );
        LocalDateTime newDate = calendarWithExceptions.getDateByDuration(date20220125, -duration);
        assertEquals(backDate, newDate);

    }

}