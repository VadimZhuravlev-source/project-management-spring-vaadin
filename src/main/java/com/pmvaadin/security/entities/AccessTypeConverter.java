package com.pmvaadin.security.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = true)
public class AccessTypeConverter implements AttributeConverter<AccessType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AccessType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public AccessType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return AccessType.of(code);
    }

}
