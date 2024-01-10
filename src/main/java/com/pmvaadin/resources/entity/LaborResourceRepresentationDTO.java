package com.pmvaadin.resources.entity;

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

}
