package com.pmvaadin.terms.calendars.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.pmvaadin.terms.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.terms.calendars.exceptiondays.ExceptionDays;

public interface Calendar {

    static long DAY_DURATION_SECONDS = 8 * 3600;

    static String getHeaderName() {
        return "Name";
    }

    Integer getId();

    void setId(Integer id);

    Integer getVersion();

    String getName();

    void setName(String name);

    CalendarSettings getSetting();

    void setSetting(CalendarSettings calendarSettings);

    List<DayOfWeekSettings> getDaysOfWeekSettings();

    void setDaysOfWeekSettings(List<DayOfWeekSettings> daysOfWeekSettings);

    List<ExceptionDays> getCalendarException();

    void setCalendarException(List<ExceptionDays> exceptionDaysList);

    LocalTime getStartTime();
    void setStartTime(LocalTime time);

    Calendar getDefaultCalendar();

    long getDuration(LocalDateTime start, LocalDateTime finish);

    LocalDateTime getDateByDuration(LocalDateTime date, long duration);

    long getDurationWithoutInitiateCache(LocalDateTime start, LocalDateTime finish);

    LocalDateTime getDateByDurationWithoutInitiateCache(LocalDateTime date, long duration);

    void initiateCacheData();

    LocalDateTime getClosestWorkingDay(LocalDateTime date);

    LocalDateTime getClosestWorkingDayWithoutInitiateCache(LocalDateTime date);

    LocalDateTime getEndOfWorkingDay(LocalDate day);

    String getRepresentation();

}
