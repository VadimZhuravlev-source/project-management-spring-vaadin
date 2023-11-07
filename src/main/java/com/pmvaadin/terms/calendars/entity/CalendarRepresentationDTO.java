package com.pmvaadin.terms.calendars.entity;

import com.pmvaadin.commonobjects.vaadin.Predefinable;

import java.time.LocalTime;
import java.util.Objects;

public record CalendarRepresentationDTO(Integer id, String name, CalendarSettings setting, LocalTime startTime, boolean isPredefined) implements CalendarRepresentation, Predefinable {
//@AllArgsConstructor
//public class CalendarRepresentationDTO implements CalendarRepresentation {

//    private final Integer id;
//    private final String name;
//    private final CalendarSettings setting;
//    private final LocalTime startTime;
//    private final boolean isPredefined;
//
//    public CalendarRepresentationDTO(Integer id, String name, Integer setting_id, LocalTime startTime, boolean isPredefined) {
//        this.id = id;
//        this.name = name;
//        this.setting = new CalendarSettingsConverter().convertToEntityAttribute(setting_id);
//        this.startTime = startTime;
//        this.isPredefined = isPredefined;
//    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalendarRepresentationDTO that)) return false;
        if (getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

//    @Override
//    public int hashCode() {
//        if (getId() == null) return super.hashCode();
//        return Objects.hash(getId());
//    }

}
