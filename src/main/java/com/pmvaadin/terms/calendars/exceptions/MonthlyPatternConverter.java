package com.pmvaadin.terms.calendars.exceptions;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter(autoApply = true)
public class MonthlyPatternConverter implements AttributeConverter<MonthlyPattern, Short> {

    @Override
    public Short convertToDatabaseColumn(MonthlyPattern pattern) {
        if (pattern == null) {
            return null;
        }
        return pattern.getCode();
    }

    @Override
    public MonthlyPattern convertToEntityAttribute(Short code) {
        if (code == null) {
            return null;
        }

        return MonthlyPattern.of(code);

    }

}
