package com.pmvaadin.terms.timeunit.entity;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Getter
@Table(name = "time_units")
public class TimeUnitImpl implements TimeUnit, HasIdentifyingFields {

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
    private BigDecimal numberOfHours = new BigDecimal("8.00");

    @Override
    public int hashCode() {
        if (id != null) return Objects.hash(id);
        else return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof TimeUnitImpl that)) {
            return false;
        }

        if (getId() == null && that.getId() == null) return false;

        return Objects.equals(getId(), that.getId());

    }

    @Override
    public String toString() {
        return name;
    }


    @Override
    public long getDuration(BigDecimal duration) {

        return duration.multiply(numberOfHours).multiply(numberOfSecondsInHour).longValue();

    }

    @Override
    public BigDecimal getDurationRepresentation(long duration) {

        return new BigDecimal(duration).divide(numberOfHours, 2, RoundingMode.CEILING)
                .divide(numberOfSecondsInHour, 2, RoundingMode.CEILING)
                .setScale(2, RoundingMode.CEILING);

    }

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

}
