package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calculation.TermCalculationData;

public interface CalendarService {

    <I> Calendar getCalendarById(I id);

    Calendar getDefaultCalendar();

    Calendar save(Calendar calendar);

    void fillCalendars(TermCalculationData termCalculationData);

}
