package com.pmvaadin.costs.labor.entities;

import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface LaborCost {

    Integer getId();
    void setId(Integer id);

    Integer getVersion();

    Date getDateOfCreation();
    Date getUpdateDate();

    String getName();
    void setName(String name);

    String getResourceName();
    void setResourceName(String name);

    Integer getLaborResourceId();
    void setLaborResourceId(Integer name);

    LaborResourceRepresentation getLaborResourceRepresentation();
    void setLaborResourceRepresentation(LaborResourceRepresentation resourceRepresentation);

    LocalDate getDay();
    void setDay(LocalDate day);

    WorkInterval getWorkIntervalInstance();

    List<WorkInterval> getIntervals();
    void setIntervals(List<WorkInterval> intervals);

    LaborCostRepresentation getRep();

}
