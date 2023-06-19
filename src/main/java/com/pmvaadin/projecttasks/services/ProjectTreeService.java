package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;

import java.util.List;
import java.util.Set;

public interface ProjectTreeService {

    List<ProjectTask> getTreeProjectTasks();
    ProjectTask sync(ProjectTask projectTask);
    void delete(List<? extends ProjectTask> projectTasks);
    Set<ProjectTask> changeLocation(Set<ProjectTask> projectTasks, ProjectTask parent, GridDropLocation dropLocation);
    Set<ProjectTask> changeSortOrder(Set<ProjectTask> tasks, Direction direction);
    Set<ProjectTask> changeLocation(Set<ProjectTask> projectTasks, Direction direction);

    void createTestCase();

    enum Direction {
        UP,
        DOWN
    }

}
