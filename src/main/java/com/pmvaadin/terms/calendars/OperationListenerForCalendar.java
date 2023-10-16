package com.pmvaadin.terms.calendars;

import com.pmvaadin.terms.calendars.entity.CalendarImpl;

import javax.persistence.PostLoad;

public class OperationListenerForCalendar {
    @PostLoad
    public void postLoad(CalendarImpl calendar) {
        calendar.setSetting(calendar.getSetting());
    }

}
