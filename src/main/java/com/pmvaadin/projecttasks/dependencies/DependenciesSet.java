package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.List;

public interface DependenciesSet {

    List<ProjectTask> getProjectTasks();
    List<Link> getLinks();
    boolean isCycle();

}
