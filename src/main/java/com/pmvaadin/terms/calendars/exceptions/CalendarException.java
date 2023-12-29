package com.pmvaadin.terms.calendars.exceptions;

import com.pmvaadin.terms.calendars.common.ExceptionLength;
import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

public interface CalendarException {

    static String getHeaderName() {
        return "Details for ";
    }

    static LocalDate getDayOfMonth(LocalDate initDate, DayOfWeek dayOfWeek, NumberOfWeek numberOfWeek) {

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

    void nullIdentifyingFields();

    Integer getId();

    void setId(Integer id);

    Integer getVersion();

    CalendarImpl getCalendar();

    void setCalendar(CalendarImpl calendar);

    String getName();

    void setName(String name);

    CalendarExceptionSetting getSetting();

    void setSetting(CalendarExceptionSetting setting);

    LocalDate getStart();

    void setStart(LocalDate start);

    LocalDate getFinish();

    void setFinish(LocalDate finish);

    RecurrenceEnd getEndByAfter();
    void setEndByAfter(RecurrenceEnd endByAfter);

    int getNumberOfOccurrence();

    void setNumberOfOccurrence(int numberOfOccurrence);

    Integer getSort();

    void setSort(Integer sort);

    RecurrencePattern getPattern();

    void setPattern(RecurrencePattern pattern);

    int getEveryNumberOfDays();

    void setEveryNumberOfDays(int everyNumberOfDays);

    DayOfWeek getEndOfWeek();
    void setEndOfWeek(DayOfWeek dayOfWeek);

    int getEveryNumberOfWeeks();

    void setEveryNumberOfWeeks(int everyNumberOfWeeks);

    boolean isEveryMonday();

    void setEveryMonday(boolean everyMonday);

    boolean isEveryTuesday();

    void setEveryTuesday(boolean everyTuesday);

    boolean isEveryWednesday();

    void setEveryWednesday(boolean everyWednesday);

    boolean isEveryThursday();

    void setEveryThursday(boolean everyThursday);

    boolean isEveryFriday();

    void setEveryFriday(boolean everyFriday);

    boolean isEverySaturday();

    void setEverySaturday(boolean everySaturday);

    boolean isEverySunday();

    void setEverySunday(boolean everySunday);

    MonthlyPattern getMonthlyPattern();

    void setMonthlyPattern(MonthlyPattern monthlyPattern);

    byte getDayOfMonth();

    void setDayOfMonth(byte dayOfMonth);

    int getEveryNumberOfMonths();

    void setEveryNumberOfMonths(int everyNumberOfMonths);

    NumberOfWeek getNumberOfWeekThe();

    void setNumberOfWeekThe(NumberOfWeek numberOfWeekThe);

    DayOfWeek getDayOfWeekThe();

    void setDayOfWeekThe(DayOfWeek dayOfWeekThe);

    int getEveryNumberOfMonthsThe();

    void setEveryNumberOfMonthsThe(int everyNumberOfMonthsThe);

    YearlyPattern getYearlyPattern();

    void setYearlyPattern(YearlyPattern yearlyPattern);

    byte getOnDateDay();

    void setOnDateDay(byte day);

    Month getOnDateMonth();

    void setOnDateMonth(Month month);

    NumberOfWeek getNumberOfWeekYear();

    void setNumberOfWeekYear(NumberOfWeek numberOfWeekYear);

    DayOfWeek getDayOfWeekYear();

    void setDayOfWeekYear(DayOfWeek dayOfWeekYear);

    Month getMonthYear();

    void setMonthYear(Month monthYear);

    Interval getIntervalInstance();

    List<Interval> getDefaultIntervals();

    List<Interval> getIntervals();
    List<Interval> getCopyOfIntervals();
    void setIntervals(List<Interval> intervals);

    Map<LocalDate, ExceptionLength> getExceptionAsDayConstraint();

}
