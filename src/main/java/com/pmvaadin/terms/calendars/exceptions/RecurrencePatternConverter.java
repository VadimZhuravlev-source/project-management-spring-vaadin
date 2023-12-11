package com.pmvaadin.terms.calendars.exceptions;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter(autoApply = true)
public class RecurrencePatternConverter implements AttributeConverter<RecurrencePattern, Short> {

    @Override
    public Short convertToDatabaseColumn(RecurrencePattern pattern) {
        if (pattern == null) {
            return null;
        }
        return pattern.getCode();
    }

    @Override
    public RecurrencePattern convertToEntityAttribute(Short code) {
        if (code == null) {
            return null;
        }

        return RecurrencePattern.of(code);

    }

}
