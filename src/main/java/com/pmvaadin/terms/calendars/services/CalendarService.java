package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calculation.TermCalculationData;

import java.util.List;
import java.util.Map;

public interface CalendarService {

    List<Calendar> getCalendars();

    <I> Calendar getCalendarById(I id);

    Calendar getDefaultCalendar();

    void saveCalendars(Calendar calendar);

    void deleteCalendar(Calendar calendar);

    void fillCalendars(TermCalculationData termCalculationData);

    Map<?, String> getRepresentationById(Iterable<?> ids);

}
