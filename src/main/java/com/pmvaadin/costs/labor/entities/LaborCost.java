package com.pmvaadin.costs.labor.entities;

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

    String getEmployeeName();
    void setEmployeeName(String name);

    Integer getLabor_resource_id();
    void setLabor_resource_id(Integer name);

    LocalDate getDay();
    void setDay(LocalDate day);

    WorkInterval getWorkIntervalInstance();

    List<WorkInterval> getIntervals();
    void setIntervals(List<WorkInterval> intervals);

    LaborCostRepresentation getRep();

}
