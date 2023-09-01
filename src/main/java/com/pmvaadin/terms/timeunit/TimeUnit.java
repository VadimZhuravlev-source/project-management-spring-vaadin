package com.pmvaadin.terms.timeunit;

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

}
