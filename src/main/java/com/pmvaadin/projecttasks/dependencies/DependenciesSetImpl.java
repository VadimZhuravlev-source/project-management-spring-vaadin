package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DependenciesSetImpl implements DependenciesSet{

    private List<ProjectTask> projectTasks = new ArrayList<>(0);
    private List<Link> links = new ArrayList<>(0);
    private boolean isCycle = false;

}
