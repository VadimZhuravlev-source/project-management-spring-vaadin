package com.pmvaadin.costs.labor.entities;

import com.pmvaadin.terms.calendars.common.Interval;

public interface WorkInterval extends Interval {

    String getTaskName();
    void setTaskName(String taskName);
    Integer getTaskId();
    void setTaskId(Integer taskId);
    LaborCost getLaborCost();
    void setLaborCost(LaborCost laborCost);
    WorkInterval getInstance();

}
