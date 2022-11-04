package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectTaskService {

    List<ProjectTask> getTreeProjectTasks();
    ProjectTask save(ProjectTask projectTask, boolean validate, boolean recalculateTerms);
    void delete(List<? extends ProjectTask> projectTasks);
    void recalculateProject();
    void changeParent(Set<? extends ProjectTask> projectTasks, ProjectTask parent);
    List<? extends ProjectTask> swap(Map<? extends ProjectTask, ? extends ProjectTask> swappedTasks);
    ProjectTask sync(ProjectTask projectTask);
    boolean validate(ProjectTask projectTask);
    Map<Integer, ProjectTask> getProjectTasksWithWbs(List<Integer> ids);

    int getChildrenCount(ProjectTask projectTask);
    boolean hasChildren(ProjectTask projectTask);
    List<ProjectTask> fetchChildren(ProjectTask projectTask);

}
