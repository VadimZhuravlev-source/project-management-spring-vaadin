package com.pmvaadin.project.dependencies;

import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.links.entities.Link;
import com.pmvaadin.project.tasks.services.ChangeHierarchyTransactionalService;
import com.pmvaadin.project.tasks.services.ProjectTaskService;

import java.util.List;

public interface DependenciesSet {

    List<ProjectTask> getProjectTasks();
    List<Link> getLinks();
    boolean isCycle();

    void fillWbs(ProjectTaskService projectTaskService);
    void fillWbs(ChangeHierarchyTransactionalService service);

}
