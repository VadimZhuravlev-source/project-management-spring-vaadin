package com.PMVaadin.PMVaadin.Entities.calendar;

import javax.persistence.PostLoad;

public class OperationListenerForCalendar {
    @PostLoad
    public void postLoad(CalendarImpl calendar) {
        calendar.setSetting(calendar.getSetting());
    }
}
