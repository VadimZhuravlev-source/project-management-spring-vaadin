package com.pmvaadin.projecttasks.resources.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.resources.entity.TaskResource;

import java.util.List;

public interface TaskResourceService {

    List<TaskResource> getLaborResources(ProjectTask projectTask);

}
