package com.pmvaadin.costs.labor.services;

import com.pmvaadin.projecttasks.entity.ProjectTaskRep;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;

import java.time.LocalDate;
import java.util.List;

public interface LaborCostService {

    List<ProjectTaskRep> getAvailableTasks(LaborResourceRepresentation laborResource, LocalDate day);

}
