package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.Set;

public interface ProjectRecalculation {

    void recalculate(Set<ProjectTask> projectTasks);

}
