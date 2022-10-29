package com.PMVaadin.PMVaadin.Calendars.Services;

import com.PMVaadin.PMVaadin.Calendars.Entity.Calendar;

import java.util.List;

public interface CalendarService {

    List<Calendar> getCalendars();

    Calendar getCalendarById(Integer id);

    void saveCalendars(Calendar calendar);

    void deleteCalendar(Calendar calendar);
}
