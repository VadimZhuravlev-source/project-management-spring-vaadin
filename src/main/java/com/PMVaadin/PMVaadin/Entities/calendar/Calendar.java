package com.PMVaadin.PMVaadin.Entities.calendar;

import java.util.List;

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
