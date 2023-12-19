package com.pmvaadin.terms.calculation;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public record TermCalculationRespondImpl(@Nonnull Set<ProjectTask> changedTasks, @Nonnull Set<ProjectTask> recalculatedProjects) implements TermCalculationRespond {

    public Set<ProjectTask> getChangedTasks() {
        return changedTasks;
    }

    public Set<ProjectTask> getRecalculatedProjects() {
        return recalculatedProjects;
    }

    public static TermCalculationRespondImpl getEmptyInstance() {
        return new TermCalculationRespondImpl(new HashSet<>(0), new HashSet<>(0));
    }

}
