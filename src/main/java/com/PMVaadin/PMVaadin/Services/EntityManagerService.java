package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;
import com.PMVaadin.PMVaadin.Entities.ProjectTask;

import java.util.List;

public interface EntityManagerService {

    List<ProjectTask> getElementsChildrenInDepth(List<? extends ProjectTask> projectTasks);

}
