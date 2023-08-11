package com.pmvaadin.calendars.entity;

import java.math.BigDecimal;
import java.util.List;

import com.pmvaadin.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.calendars.exceptiondays.ExceptionDays;

public interface Calendar {

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

    BigDecimal getStartTime();
    void setStartTime(BigDecimal time);

    Calendar getDefaultCalendar();

}
