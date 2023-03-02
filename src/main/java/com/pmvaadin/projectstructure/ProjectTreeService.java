package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectTreeService {

    List<ProjectTask> getTreeProjectTasks();
    ProjectTask sync(ProjectTask projectTask);
    void delete(List<? extends ProjectTask> projectTasks);
    void changeParent(Set<ProjectTask> projectTasks, ProjectTask parent);
    List<ProjectTask> swap(Map<ProjectTask, ProjectTask> swappedTasks);
    void increaseTaskLevel(Set<ProjectTask> projectTasks);
    void decreaseTaskLevel(Set<ProjectTask> projectTasks);

    void createTestCase();

}
