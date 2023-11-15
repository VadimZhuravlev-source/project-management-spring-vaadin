package com.pmvaadin.terms.calendars.exceptions;

import com.pmvaadin.terms.calendars.common.Interval;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public interface CalendarException {

    void nullIdentifyingFields();

    List<Interval> getIntervals();

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

    int getFinishAfter();

    void setFinishAfter(int finishAfter);

    Integer getSort();

    void setSort(Integer sort);

    RecurrencePattern getPattern();

    void setPattern(RecurrencePattern pattern);

    int getNumberOfDays();

    void setNumberOfDays(int numberOfDays);

    int getNumberOfWeeks();

    void setNumberOfWeeks(int numberOfWeeks);

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

    int getNumberOfMonth();

    void setNumberOfMonth(int numberOfMonth);

    NumberOfWeek getNumberOfWeekThe();

    void setNumberOfWeekThe(NumberOfWeek numberOfWeekThe);

    DayOfWeek getDayOfWeekThe();

    void setDayOfWeekThe(DayOfWeek dayOfWeekThe);

    int getNumberOfMonthThe();

    void setNumberOfMonthThe(int numberOfMonthThe);

    YearlyPattern getYearlyPattern();

    void setYearlyPattern(YearlyPattern yearlyPattern);

    LocalDate getOnDate();

    void setOnDate(LocalDate onDate);

    NumberOfWeek getNumberOfWeekYear();

    void setNumberOfWeekYear(NumberOfWeek numberOfWeekYear);

    DayOfWeek getDayOfWeekYear();

    void setDayOfWeekYear(DayOfWeek dayOfWeekYear);

    Month getMonthYear();

    void setMonthYear(Month monthYear);

    Interval getIntervalInstance();

    void setIntervals(List<CalendarExceptionInterval> intervals);

}
