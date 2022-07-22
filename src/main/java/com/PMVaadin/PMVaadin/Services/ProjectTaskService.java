package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.ProjectStructure.TreeItem;

import java.util.List;
import java.util.Set;

public interface ProjectTaskService {

    TreeItem<ProjectTask> getTreeProjectTasks() throws Exception;
    ProjectTask saveTask(ProjectTask projectTask) throws Exception;
    void deleteTasks(List<? extends ProjectTask> projectTasks);
    void recalculateProject();
    void setNewParentOfTheTasks(Set<? extends ProjectTask> projectTasks, ProjectTask parent);

}
