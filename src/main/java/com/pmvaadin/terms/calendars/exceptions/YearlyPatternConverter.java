package com.pmvaadin.terms.calendars.exceptions;

import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Component
@Converter(autoApply = true)
public class YearlyPatternConverter implements AttributeConverter<YearlyPattern, Short> {

    @Override
    public Short convertToDatabaseColumn(YearlyPattern pattern) {
        if (pattern == null) {
            return null;
        }
        return pattern.getCode();
    }

    @Override
    public YearlyPattern convertToEntityAttribute(Short code) {
        if (code == null) {
            return null;
        }

        return YearlyPattern.of(code);

    }

}
