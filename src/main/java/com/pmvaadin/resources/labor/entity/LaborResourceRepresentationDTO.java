package com.pmvaadin.resources.labor.entity;

import java.util.Objects;

public record LaborResourceRepresentationDTO(Integer id, String name) implements LaborResourceRepresentation {

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaborResourceRepresentationDTO that)) return false;
        if (getId() == null || that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
