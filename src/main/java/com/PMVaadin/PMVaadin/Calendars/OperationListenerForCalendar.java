package com.PMVaadin.PMVaadin.Calendars;

import com.PMVaadin.PMVaadin.Calendars.Entity.CalendarImpl;

import javax.persistence.PostLoad;

public class OperationListenerForCalendar {
    @PostLoad
    public void postLoad(CalendarImpl calendar) {
        calendar.setSetting(calendar.getSetting());
    }
}
