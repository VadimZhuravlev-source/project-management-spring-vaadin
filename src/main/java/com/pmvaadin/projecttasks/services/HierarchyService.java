package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface HierarchyService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(List<?> projectTasks);
    List<ProjectTask> getParentsOfParent(ProjectTask projectTask);

}
