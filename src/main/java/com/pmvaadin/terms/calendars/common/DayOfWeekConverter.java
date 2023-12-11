package com.pmvaadin.terms.calendars.common;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;

@Component
@Converter(autoApply = true)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(Integer code) {

        if (code == null) {
            return null;
        }

        return DayOfWeek.of(code);
    }

}
