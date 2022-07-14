package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.ProjectStructure.TreeItem;

import java.util.List;

public interface ProjectTaskService {

    TreeItem<ProjectTask> getTreeProjectTasks() throws Exception;
    ProjectTask saveTask(ProjectTask projectTask) throws Exception;
    void deleteTasks(List<ProjectTask> projectTasks) throws Exception;
    void recalculateProject();

}
