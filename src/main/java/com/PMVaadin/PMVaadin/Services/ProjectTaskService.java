package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;
import com.PMVaadin.PMVaadin.ProjectStructure.TreeItem;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectTaskService {

    TreeItem<ProjectTask> getTreeProjectTasks();
    ProjectTask saveTask(ProjectTask projectTask);
    void deleteTasks(List<? extends ProjectTask> projectTasks);
    void recalculateProject();
    void setNewParentOfTheTasks(Set<? extends ProjectTask> projectTasks, ProjectTask parent);
    List<ProjectTask> swapTasks(Map<ProjectTask, ProjectTask> swappedTasks);

}
