package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;

import java.util.List;

public interface ProjectTaskService {

    List<ProjectTask> getProjectTasks() throws Exception;
    ProjectTask saveTask(ProjectTask projectTask) throws Exception;
    void deleteTasks(List<ProjectTask> projectTasks) throws Exception;

}
