package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface EntityManagerService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(List<Integer> projectTasks);
    List<ProjectTask> getParentsOfParent(ProjectTask projectTask);

}
