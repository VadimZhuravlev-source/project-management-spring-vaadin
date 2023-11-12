package com.pmvaadin.terms.calendars.exceptions;

import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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

//        return Stream.of(IntervalSettings.values())
//                .filter(c -> c.getCode().equals(code))
//                .findFirst()
//                .orElseThrow(IllegalArgumentException::new);
    }

}
