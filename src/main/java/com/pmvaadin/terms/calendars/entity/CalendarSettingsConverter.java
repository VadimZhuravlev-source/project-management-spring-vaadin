package com.pmvaadin.terms.calendars.entity;

import javax.persistence.AttributeConverter;
import java.util.stream.Stream;

public class CalendarSettingsConverter implements AttributeConverter<CalendarSettings, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CalendarSettings calendarSettings) {
        if (calendarSettings == null) {
            return null;
        }
        return calendarSettings.getCode();
    }

    @Override
    public CalendarSettings convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return Stream.of(CalendarSettings.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
