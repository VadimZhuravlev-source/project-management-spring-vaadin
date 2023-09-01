package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calculation.TermCalculationData;

import java.util.List;

public interface CalendarService {

    List<Calendar> getCalendars();

    Calendar getCalendarById(Integer id);

    void saveCalendars(Calendar calendar);

    void deleteCalendar(Calendar calendar);

    void fillCalendars(TermCalculationData termCalculationData);

}
