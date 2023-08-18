package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.Set;

public record TermCalculationRespondImpl(Set<ProjectTask> changedTasks, Set<ProjectTask> recalculatedProjects) implements TermCalculationRespond {

    public Set<ProjectTask> getChangedTasks() {
        return changedTasks;
    }

    public Set<ProjectTask> getRecalculatedProjects() {
        return recalculatedProjects;
    }

}
