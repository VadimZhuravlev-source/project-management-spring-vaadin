package com.pmvaadin.projecttasks.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class ScheduleModeConverter implements AttributeConverter<ScheduleMode, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ScheduleMode planningType) {
        if (planningType == null) {
            return null;
        }
        return planningType.getCode();
    }

    @Override
    public ScheduleMode convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return ScheduleMode.of(code);
    }

}
