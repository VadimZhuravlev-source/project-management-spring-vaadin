package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectDataService {

    ProjectData getProjectData();
    ProjectTask saveTask(ProjectTask projectTask);
    void deleteTasks(List<? extends ProjectTask> projectTasks);
    void setNewParentOfTheTasks(Set<? extends ProjectTask> projectTasks, ProjectTask parent);
    List<ProjectTask> swapTasks(Map<ProjectTask, ProjectTask> swappedTasks);

}
