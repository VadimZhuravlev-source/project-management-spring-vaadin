package com.PMVaadin.PMVaadin.Calendars.Entity;

import java.util.List;

import com.PMVaadin.PMVaadin.Calendars.DayOfWeekSettings.DayOfWeekSettings;
import com.PMVaadin.PMVaadin.Calendars.ExceptionDays.*;

public interface Calendar {
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
}
