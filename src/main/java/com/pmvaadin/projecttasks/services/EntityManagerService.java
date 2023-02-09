package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.List;

public interface EntityManagerService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(List<?> projectTasks);
    List<ProjectTask> getParentsOfParent(ProjectTask projectTask);

    <I> List<Link> getAllDependencies(I parentId, List<?> ids);

}
