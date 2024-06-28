package com.pmvaadin.project.tasks.services;

import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;

import java.util.List;
import java.util.Set;

public interface ProjectTreeService {

    List<ProjectTask> getTreeProjectTasks();
    ProjectTask sync(ProjectTask projectTask);
    void delete(List<? extends ProjectTask> projectTasks);
    void changeLocation(Set<ProjectTask> projectTasks, ProjectTask parent, GridDropLocation dropLocation);
    void changeSortOrder(Set<ProjectTask> tasks, Direction direction);
    void changeLocation(Set<ProjectTask> projectTasks, Direction direction);

    void createTestCase();

    enum Direction {
        UP,
        DOWN
    }

}
