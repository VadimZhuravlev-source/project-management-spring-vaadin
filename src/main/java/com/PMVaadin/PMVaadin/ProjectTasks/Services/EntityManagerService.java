package com.PMVaadin.PMVaadin.ProjectTasks.Services;

import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;

import java.util.List;

public interface EntityManagerService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(ProjectTask projectTask);

}
