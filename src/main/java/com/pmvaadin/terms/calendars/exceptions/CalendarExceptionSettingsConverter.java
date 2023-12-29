package com.pmvaadin.terms.calendars.exceptions;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter(autoApply = true)
public class CalendarExceptionSettingsConverter implements AttributeConverter<CalendarExceptionSetting, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CalendarExceptionSetting settings) {
        if (settings == null) {
            return null;
        }
        return settings.getCode();
    }

    @Override
    public CalendarExceptionSetting convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return CalendarExceptionSetting.of(code);

    }

}
