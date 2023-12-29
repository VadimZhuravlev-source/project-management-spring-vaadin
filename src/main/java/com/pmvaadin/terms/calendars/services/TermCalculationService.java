package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.terms.calendars.entity.Calendar;

public interface TermCalculationService {
    void fillCalendars(TermCalculationData termCalculationData);

    Calendar getDefaultCalendar();

}
