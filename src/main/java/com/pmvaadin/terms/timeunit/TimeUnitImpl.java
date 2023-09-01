package com.pmvaadin.terms.timeunit;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
public class TimeUnitImpl implements TimeUnit {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    private String name;

    private boolean predefined;

    @Setter
    @Column(name = "number_of_hours")
    private BigDecimal numberOfHours;

}
