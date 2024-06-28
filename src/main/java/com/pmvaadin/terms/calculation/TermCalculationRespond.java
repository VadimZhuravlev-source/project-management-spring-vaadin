package com.pmvaadin.terms.calculation;

import com.pmvaadin.project.tasks.entity.ProjectTask;

import java.util.Set;

public interface TermCalculationRespond {

    Set<ProjectTask> getChangedTasks();
    Set<ProjectTask> getRecalculatedProjects();

}
