package com.pmvaadin.costs.labor.entities;

import com.pmvaadin.projecttasks.entity.ProjectTaskRep;

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

    Integer getEmployeeId();
    void setEmployeeId(Integer name);

    LocalDate getDay();
    void setDay(LocalDate day);

    List<WorkInterval> getIntervals();
    void setIntervals(List<WorkInterval> intervals);

    List<ProjectTaskRep> getAssignedTasks();
    void setAssignedTasks(List<ProjectTaskRep> assignedTasks);

    LaborCostRepresentation getRep();

}
