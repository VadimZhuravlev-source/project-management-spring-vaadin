package com.pmvaadin.terms.calendars.entity;

public interface CalendarRepresentation {

    Integer getId();
    String getName();
    CalendarSettings getSettings();
    boolean isPredefined();

}
