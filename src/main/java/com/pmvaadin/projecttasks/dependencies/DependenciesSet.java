package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.services.ChangeHierarchyTransactionalService;
import com.pmvaadin.projecttasks.services.ProjectTaskService;

import java.util.List;

public interface DependenciesSet {

    List<ProjectTask> getProjectTasks();
    List<Link> getLinks();
    boolean isCycle();

    void fillWbs(ProjectTaskService projectTaskService);
    void fillWbs(ChangeHierarchyTransactionalService service);

}
