package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Override
    public void fillWbs(ProjectTaskService projectTaskService) {

        if (projectTasks.isEmpty()) return;

        var cycledPTIds = projectTasks.stream().map(ProjectTask::getId).toList();
        Map<?, ProjectTask> projectTasksMap = projectTaskService.getProjectTasksByIdWithFilledWbs(cycledPTIds);
        projectTasks.clear();
        projectTasks.addAll(projectTasksMap.values());

    }

}
