package com.pmvaadin.terms.calculation;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.Set;

public interface TermCalculationRespond {

    Set<ProjectTask> getChangedTasks();
    Set<ProjectTask> getRecalculatedProjects();

}
