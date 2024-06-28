package com.pmvaadin.project.tasks.services;

import com.pmvaadin.project.data.ProjectTaskData;
import com.pmvaadin.project.tasks.entity.ProjectTask;

public interface ProjectTaskDataService {

    ProjectTaskData save(ProjectTaskData projectTaskData);

    ProjectTaskData read(ProjectTask projectTask);

}
