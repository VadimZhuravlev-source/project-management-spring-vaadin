package com.pmvaadin.calendars.entity;

import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import com.pmvaadin.terms.calendars.entity.CalendarSettings;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionImpl;
import com.pmvaadin.terms.calendars.exceptions.CalendarExceptionSetting;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarImplTest {

    private final int secondInHour = 3600;

    private final LocalDateTime date20220125 = LocalDateTime.of(2022, 1, 25, 11, 23, 11);

    private final LocalDateTime date20211223 = LocalDateTime.of(2021, 12, 23, 11, 56, 23);

    private final LocalDate shortWorkingDay = LocalDate.of(2021, 12, 31);

    private final CalendarImpl calendar = new CalendarImpl();
    private final CalendarImpl calendarWithExceptions = new CalendarImpl();
    private final CalendarImpl calendarNightShift = new CalendarImpl();
    private final CalendarImpl calendar24 = new CalendarImpl();

    {

        calendar.setWorkingWeeks(calendar.getWorkingWeeks());

        calendarNightShift.setSetting(CalendarSettings.NIGHT_SHIFT);
        calendarNightShift.setWorkingWeeks(calendarNightShift.getWorkingWeeks());

        calendar24.setSetting(CalendarSettings.FULL_DAY);
        calendar24.setWorkingWeeks(calendar24.getWorkingWeeks());

        fillExceptions(calendarWithExceptions);
//        List<ExceptionDay> exceptions = calendarWithExceptions.getCalendarException();
//        exceptions.addAll(getExceptions());

    }

    private void fillExceptions(CalendarImpl calendar) {
        var exceptions = new ArrayList<CalendarExceptionImpl>();

        var exception = (CalendarExceptionImpl) calendar.getCalendarExceptionInstance();
        exception.setStart(LocalDate.of(2022, 1, 3));
        exception.setNumberOfOccurrence(8);
        var finish = exception.getExceptionAsDayConstraint().keySet().stream().max(Comparator.naturalOrder());
        exception.setFinish(finish.get());
        exceptions.add(exception);

        exception = (CalendarExceptionImpl) calendar.getCalendarExceptionInstance();
        exception.setStart(shortWorkingDay);
        exception.setSetting(CalendarExceptionSetting.WORKING_TIMES);
        var intervals = new ArrayList<Interval>(2);
        var interval = exception.getIntervalInstance();
        interval.setFrom(LocalTime.of(8, 0));
        interval.setTo(LocalTime.of(12, 0));
        intervals.add(interval);
        interval = exception.getIntervalInstance();
        interval.setFrom(LocalTime.of(13, 0));
        interval.setTo(LocalTime.of(16, 0));
        intervals.add(interval);
        exception.setIntervals(intervals);
        exception.setNumberOfOccurrence(1);
        finish = exception.getExceptionAsDayConstraint().keySet().stream().max(Comparator.naturalOrder());
        exception.setFinish(finish.get());
        exceptions.add(exception);

        calendar.setExceptions(exceptions);

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
        LocalDateTime date = LocalDateTime.of(2023, 8, 29, 8, 0);
        LocalDateTime aheadDate = date.plusHours(9);
        LocalDateTime newDate = calendar.getDateByDuration(date, duration);
        assertEquals(aheadDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsMinusOneDay() {

        long duration = - 8L * secondInHour;
        LocalDateTime date = LocalDateTime.of(2023, 8, 29, 17, 0);
        LocalDateTime aheadDate = date.minusHours(9);
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
        LocalDateTime aheadDate = LocalDateTime.of(day, LocalTime.of(11, 0));
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
        LocalDateTime aheadDate = LocalDateTime.of(day, LocalTime.of(11, 0));
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

        LocalTime newTime = LocalTime.of(13, 0);
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
                date20211223.toLocalTime().plusHours(2)
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
        LocalDateTime backDate = LocalDateTime.of(2021, 12, 31, 14, 0);
        LocalDateTime date = LocalDateTime.of(2022, 1, 13, 10, 0);

        LocalDateTime newDate = calendarWithExceptions.getDateByDuration(date, duration);
        assertEquals(backDate, newDate);

    }

    @Test
    void getDateByDuration_WhereDurationIsYearForCalendarNightShift() {

        long duration = 365L * 8L * secondInHour;
        LocalDateTime date = LocalDateTime.of(2023, 1, 13, 10, 0);
        LocalDateTime backDate = LocalDateTime.of(2024, 6, 7, 8, 0);

        LocalDateTime newDate = calendarNightShift.getDateByDuration(date, duration);
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

    @Test
    void checkFullDayCalendarReturnNextDay() {
        var date = LocalDateTime.of(2023, 11, 20, 0, 0);
        var newDate = calendar24.getDateByDuration(date, Calendar.FULL_DAY_SECONDS);
        date = date.plusDays(1);
        assertEquals(date, newDate);

        newDate = calendar24.getDateByDuration(date, - Calendar.FULL_DAY_SECONDS);
        date = date.minusDays(1);
        assertEquals(date, newDate);

    }

    @Test
    void testStartDayInMiddleOfFirstShift() {
        var date = LocalDateTime.of(2023, 11, 20, 11, 0);
        var newDate = calendar.getDateByDuration(date, 3L * secondInHour);
        date = date.plusHours(4);
        assertEquals(date, newDate);

        newDate = calendar.getDateByDuration(date, - 3L * secondInHour);
        date = date.minusHours(4);
        assertEquals(date, newDate);

    }

}