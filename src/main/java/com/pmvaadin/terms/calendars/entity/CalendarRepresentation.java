package com.pmvaadin.terms.calendars.entity;

import java.time.LocalTime;

public interface CalendarRepresentation {

    Integer getId();
    String getName();
    CalendarSettings getSettings();
    LocalTime getStartTime();

}
