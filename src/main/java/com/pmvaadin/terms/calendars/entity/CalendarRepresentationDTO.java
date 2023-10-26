package com.pmvaadin.terms.calendars.entity;

import java.time.LocalTime;

public record CalendarRepresentationDTO(Integer id, String name, CalendarSettings setting, LocalTime startTime, boolean isPredefined) implements CalendarRepresentation {

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CalendarSettings getSettings() {
        return setting;
    }

    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    @Override
    public boolean isPredefined() {
        return isPredefined;
    }

}
