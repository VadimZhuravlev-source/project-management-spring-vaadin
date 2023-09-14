package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.entity.ProjectTask;

public interface ProjectTaskDataService {

    ProjectTaskData save(ProjectTaskData projectTaskData);

    ProjectTaskData read(ProjectTask projectTask);

}
