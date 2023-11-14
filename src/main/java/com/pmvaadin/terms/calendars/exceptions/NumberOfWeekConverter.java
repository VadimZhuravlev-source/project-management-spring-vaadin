package com.pmvaadin.terms.calendars.exceptions;

import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Component
@Converter(autoApply = true)
public class NumberOfWeekConverter implements AttributeConverter<NumberOfWeek, Short> {

    @Override
    public Short convertToDatabaseColumn(NumberOfWeek pattern) {
        if (pattern == null) {
            return null;
        }
        return pattern.getCode();
    }

    @Override
    public NumberOfWeek convertToEntityAttribute(Short code) {
        if (code == null) {
            return null;
        }

        return NumberOfWeek.of(code);

    }

}
