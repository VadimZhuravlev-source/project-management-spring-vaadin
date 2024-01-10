package com.pmvaadin.terms.timeunit.entity;

import java.math.BigDecimal;

public interface TimeUnitRepresentation {

    Integer getId();
    String getName();
    boolean isPredefined();
    BigDecimal getNumberOfHours();

}
