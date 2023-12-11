package com.pmvaadin.terms.calendars.exceptions;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Month;

@Component
@Converter(autoApply = true)
public class MonthConverter implements AttributeConverter<Month, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Month pattern) {
        if (pattern == null) {
            return null;
        }
        return pattern.getValue();
    }

    @Override
    public Month convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return Month.of(code);

    }

}
