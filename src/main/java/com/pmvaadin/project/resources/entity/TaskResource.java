package com.pmvaadin.project.resources.entity;

import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;

import java.math.BigDecimal;

public interface TaskResource {

    Integer getId();

    void setId(Integer id);

    Integer getVersion() ;

    Integer getProjectTaskId();

    void setProjectTaskId(Integer projectTaskId);

    Integer getResourceId();

    void setResourceId(Integer resourceId);

    BigDecimal getDuration();

    void setDuration(BigDecimal duration);

    int getSort();

    void setSort(int sort);

    LaborResourceRepresentation getLaborResource();
    void setLaborResource(LaborResourceRepresentation laborResource);

    TaskResource copy();

}
