package com.pmvaadin.projecttasks.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
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

        return Stream.of(ScheduleMode.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
