package com.pmvaadin.terms.timeunit.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "time_unit")
public class TimeUnitImpl implements TimeUnit {

    private static final BigDecimal numberOfSecondsInHour = new BigDecimal(3600);

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

    @Override
    public long getDuration(BigDecimal duration) {

        return duration.multiply(numberOfHours).multiply(numberOfSecondsInHour).longValue();

    }

    @Override
    public BigDecimal getDurationRepresentation(long duration) {

        return new BigDecimal(duration).divide(numberOfHours).divide(numberOfSecondsInHour);

    }

}
