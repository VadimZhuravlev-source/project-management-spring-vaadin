package com.pmvaadin.resources.entity;

public interface LaborResource {

    Integer getId();
    void setId(Integer id);

    Integer getVersion();

    String getName();
    void setName(String name);

    LaborResourceRepresentation getRep();

}
