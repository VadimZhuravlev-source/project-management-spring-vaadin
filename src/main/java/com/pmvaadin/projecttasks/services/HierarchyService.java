package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.Collection;
import java.util.List;

public interface HierarchyService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(Collection<?> projectTasks);
    List<ProjectTask> getParentsOfParent(ProjectTask projectTask);

}
