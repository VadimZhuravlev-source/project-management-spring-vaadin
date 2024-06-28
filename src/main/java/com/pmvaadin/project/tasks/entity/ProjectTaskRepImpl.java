package com.pmvaadin.project.tasks.entity;

public record ProjectTaskRepImpl(Integer id, String rep) implements ProjectTaskRep {

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getRep() {
        return rep;
    }

}
