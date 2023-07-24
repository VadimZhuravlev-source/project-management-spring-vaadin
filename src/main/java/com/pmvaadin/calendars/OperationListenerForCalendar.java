package com.pmvaadin.calendars;

import com.pmvaadin.calendars.entity.CalendarImpl;

import javax.persistence.PostLoad;

public class OperationListenerForCalendar {
    @PostLoad
    public void postLoad(CalendarImpl calendar) {
        calendar.setSetting(calendar.getSetting());
    }

}
