package com.pmvaadin.projecttasks.links.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class LinkTypeConverter implements AttributeConverter<LinkType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LinkType linkType) {
        if (linkType == null) {
            return null;
        }
        return linkType.getCode();
    }

    @Override
    public LinkType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return Stream.of(LinkType.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
