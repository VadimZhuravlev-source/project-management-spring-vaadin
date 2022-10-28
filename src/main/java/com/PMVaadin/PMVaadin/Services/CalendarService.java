package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.calendar.Calendar;
import com.PMVaadin.PMVaadin.Entities.calendar.CalendarImpl;

import java.util.List;

public interface CalendarService {

    List<CalendarImpl> getCalendars();

    CalendarImpl getCalendarById(Integer id);

    void saveCalendars(CalendarImpl calendar);

    void deleteCalendar(CalendarImpl calendar);
}
