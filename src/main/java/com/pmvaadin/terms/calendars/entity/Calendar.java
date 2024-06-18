package com.pmvaadin.terms.calendars.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.pmvaadin.terms.calendars.exceptions.CalendarException;
import com.pmvaadin.terms.calendars.workingweeks.WorkingWeek;

public interface Calendar extends CalendarRepresentation {

    long DAY_DURATION_SECONDS = 8 * 3600;

    int FULL_DAY_SECONDS = 24 * 3600;

    int SECONDS_IN_HOUR = 3600;

    static String getCalendarHeader() {
        return "Calendar";
    }

    static String getHeaderName() {
        return "Name";
    }

    Integer getId();

    void setId(Integer id);

    Integer getVersion();
    void setVersion(Integer version);

    String getName();

    void setName(String name);

    CalendarSettings getSetting();

    void setSetting(CalendarSettings calendarSettings);

    Calendar getDefaultCalendar();

    long getDuration(LocalDateTime start, LocalDateTime finish);

    LocalDateTime getDateByDuration(LocalDateTime date, long duration);

    long getDurationWithoutInitiateCache(LocalDateTime start, LocalDateTime finish);

    LocalDateTime getDateByDurationWithoutInitiateCache(LocalDateTime date, long duration);

    void initiateCacheData();

    LocalDateTime getClosestWorkingDay(LocalDateTime date);

    LocalDateTime getClosestWorkingDayWithoutInitiateCache(LocalDateTime date);

    LocalDateTime getEndOfWorkingDay(LocalDate day);

    boolean isPredefined();
    void setPredefined(boolean isPredefined);

    DayOfWeek getEndOfWeek();
    void setEndOfWeek(DayOfWeek dayOfWeek);

    boolean isNew();

    WorkingWeek getWorkingWeekInstance();
    CalendarException getCalendarExceptionInstance();

    List<CalendarException> getCalendarExceptions();
    void setCalendarExceptions(List<CalendarException> calendarExceptions);

    List<WorkingWeek> getWorkingWeeks();
    void setWorkingWeeks(List<WorkingWeek> workingWeeks);

    void fillWorkingWeekSort();
    void fillExceptionSort();

}
