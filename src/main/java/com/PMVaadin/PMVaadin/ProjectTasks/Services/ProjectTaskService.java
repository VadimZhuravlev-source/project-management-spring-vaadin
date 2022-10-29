package com.PMVaadin.PMVaadin.ProjectTasks.Services;

import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectTaskService {

    List<ProjectTask> getTreeProjectTasks();
    ProjectTask saveTask(ProjectTask projectTask);
    void deleteTasks(List<? extends ProjectTask> projectTasks);
    void recalculateProject();
    void setNewParentOfTheTasks(Set<? extends ProjectTask> projectTasks, ProjectTask parent);
    List<ProjectTask> swapTasks(Map<ProjectTask, ProjectTask> swappedTasks);

    ProjectTask refreshTask(ProjectTask projectTask);

}
