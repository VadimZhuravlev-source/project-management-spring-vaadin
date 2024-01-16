package com.pmvaadin.terms.calendars.entity;

public interface CalendarRepresentation {

    Integer getId();
    String getName();
    CalendarSettings getSetting();
    boolean isPredefined();

}
