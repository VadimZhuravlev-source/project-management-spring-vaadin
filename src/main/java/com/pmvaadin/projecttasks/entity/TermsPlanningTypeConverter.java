package com.pmvaadin.projecttasks.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class TermsPlanningTypeConverter implements AttributeConverter<TermsPlanningType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TermsPlanningType planningType) {
        if (planningType == null) {
            return null;
        }
        return planningType.getCode();
    }

    @Override
    public TermsPlanningType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return Stream.of(TermsPlanningType.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
