package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.terms.calendars.entity.CalendarRepresentation;

import java.util.List;

public interface CalendarService {

    List<Calendar> getCalendars();

    <I> Calendar getCalendarById(I id);

    Calendar getCalendar(CalendarRepresentation calendarRep);

    Calendar getDefaultCalendar();

    void saveCalendars(Calendar calendar);

    void deleteCalendar(Calendar calendar);

    void fillCalendars(TermCalculationData termCalculationData);

}
