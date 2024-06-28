package com.pmvaadin.project.tasks.services;

import com.pmvaadin.project.tasks.entity.ProjectTask;

import java.util.Collection;
import java.util.List;

public interface HierarchyService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(Collection<?> ids);
    List<ProjectTask> getParentsOfParent(ProjectTask projectTask);

}
