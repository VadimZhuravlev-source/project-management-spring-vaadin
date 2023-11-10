package com.pmvaadin.terms.calendars.workingweeks;

import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Component
@Converter(autoApply = true)
public class IntervalSettingsConverter implements AttributeConverter<IntervalSettings, Integer> {

    @Override
    public Integer convertToDatabaseColumn(IntervalSettings intervalSettings) {
        if (intervalSettings == null) {
            return null;
        }
        return intervalSettings.getCode();
    }

    @Override
    public IntervalSettings convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return Stream.of(IntervalSettings.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
