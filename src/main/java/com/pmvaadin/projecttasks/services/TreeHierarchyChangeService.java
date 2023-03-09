package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface TreeHierarchyChangeService {

    List<ProjectTask> fetchChildren(ProjectTask projectTask);

}
