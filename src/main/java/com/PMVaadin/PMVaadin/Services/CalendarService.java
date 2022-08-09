package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.Calendar.Calendar;

import java.util.List;

public interface CalendarService {

    List<Calendar> getCalendars();

    Calendar getCalendarById(Integer id);

    void saveCalendars(Calendar calendar);

    void deleteCalendar(Calendar calendar);
}
