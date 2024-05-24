package com.pmvaadin.costs.labor.entities;

import java.time.LocalTime;

public interface WorkInterval {
    Integer getId();
    void setId(Integer id);
    Integer getVersion();
    String getName();
    void setName(String id);
    Integer getTaskId();
    void setTaskId(Integer taskId);
    LaborCost getLaborCost();
    void setLaborCost(LaborCost laborCost);
    LocalTime getFrom();
    void setFrom(LocalTime laborCost);
    LocalTime getTo();
    void setTo(LocalTime laborCost);
    int getDuration();
    void setDuration(int laborCost);
    WorkInterval getInstance();
    void fillDuration();
}
