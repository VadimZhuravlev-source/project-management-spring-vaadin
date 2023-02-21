package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class DependenciesSetImpl implements DependenciesSet{

    private List<ProjectTask> projectTasks;
    private List<Link> links;
    private boolean isCycle;

    public DependenciesSetImpl() {
        projectTasks = new ArrayList<>(0);
        links = new ArrayList<>(0);
        isCycle = false;
    }

}
