package com.pmvaadin.security.entities;

import com.pmvaadin.resources.entity.LaborResourceRepresentationDTO;

import java.util.Objects;

public record UserRepresentationDTO(Integer id, String name, boolean isActive, boolean isPredefined) implements UserRepresentation {

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
        if (!(o instanceof UserRepresentationDTO that)) return false;
        if (getId() == null || that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
