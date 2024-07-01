package com.pmvaadin.project.resources.labor.services;

import com.pmvaadin.project.data.ProjectTaskData;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.resources.labor.entity.TaskResource;

import java.util.List;

public interface TaskResourceService {

    List<TaskResource> getLaborResources(ProjectTask projectTask);
    List<TaskResource> save(ProjectTaskData projectTaskData);
    boolean validate(List<TaskResource> resources);

}
