package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.List;

public interface DependenciesService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(List<?> projectTasks);
    List<ProjectTask> getParentsOfParent(ProjectTask projectTask);

    <I, L> DependenciesSet getAllDependencies(I parentId, List<?> ids);

}
