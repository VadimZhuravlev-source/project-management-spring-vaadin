package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectDataService {

    List<ProjectTask> getTreeProjectTasks();
    ProjectData getProjectData(ProjectTask projectTask);
    ProjectTask saveTask(ProjectTask projectTask);
    void deleteTasks(List<? extends ProjectTask> projectTasks);
    void setNewParentOfTheTasks(Set<? extends ProjectTask> projectTasks, ProjectTask parent);
    List<ProjectTask> swapTasks(Map<ProjectTask, ProjectTask> swappedTasks);
    ProjectTask refreshTask(ProjectTask projectTask);

    // Links
    List<Link> getLinks(ProjectTask projectTask);

}
