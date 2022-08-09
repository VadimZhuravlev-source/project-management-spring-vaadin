package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;

import java.util.List;

public interface EntityManagerService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(List<? extends ProjectTask> projectTasks);
    List<ProjectTask> getParentsOfParent(ProjectTask projectTask);

}
