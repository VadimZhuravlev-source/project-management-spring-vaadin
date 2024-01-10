package com.pmvaadin.terms.timeunit.entity;

import java.math.BigDecimal;

public interface TimeUnit {

    Integer getId();

    void setId(Integer id);

    Integer getVersion();

    String getName();

    void setName(String name);

    boolean isPredefined();

    BigDecimal getNumberOfHours();

    void setNumberOfHours(BigDecimal numberOfHours);

    long getDuration(BigDecimal duration);
    BigDecimal getDurationRepresentation(long duration);

    TimeUnitRepresentation getRep();

}
