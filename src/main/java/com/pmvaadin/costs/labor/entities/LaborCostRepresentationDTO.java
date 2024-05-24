package com.pmvaadin.costs.labor.entities;

import java.time.LocalDate;
import java.util.Objects;

public record LaborCostRepresentationDTO(Integer id, String name, LocalDate day) implements LaborCostRepresentation {

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public LocalDate getDay() {
        return day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaborCostRepresentationDTO that)) return false;
        if (getId() == null || that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
