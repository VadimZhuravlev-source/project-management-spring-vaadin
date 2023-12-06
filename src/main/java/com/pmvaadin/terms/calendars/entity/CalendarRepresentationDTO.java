package com.pmvaadin.terms.calendars.entity;

import java.util.Objects;

public record CalendarRepresentationDTO(Integer id, String name, CalendarSettings setting, boolean isPredefined) implements CalendarRepresentation{

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
    public boolean isPredefined() {
        return isPredefined;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalendarRepresentationDTO that)) return false;
        if (getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

}
