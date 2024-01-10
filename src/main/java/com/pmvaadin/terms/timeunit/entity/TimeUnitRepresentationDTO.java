package com.pmvaadin.terms.timeunit.entity;

import java.math.BigDecimal;

public record TimeUnitRepresentationDTO(Integer id, String name, boolean predefined, BigDecimal numberOfHours) implements TimeUnitRepresentation {

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPredefined() {
        return predefined;
    }

    @Override
    public BigDecimal getNumberOfHours() {
        return numberOfHours;
    }

    @Override
    public String toString() {
        return name;
    }
}
