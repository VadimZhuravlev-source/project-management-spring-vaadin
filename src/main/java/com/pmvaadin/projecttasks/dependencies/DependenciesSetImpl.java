package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.services.ChangeHierarchyTransactionalService;
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
    public void fillWbs(ProjectTaskService service) {

//        if (projectTasks.isEmpty()) return;
//
//        var cycledPTIds = projectTasks.stream().map(ProjectTask::getId).toList();
//        Map<?, ProjectTask> projectTasksMap = projectTaskService.getProjectTasksByIdWithFilledWbs(cycledPTIds);
//        projectTasks.clear();
//        projectTasks.addAll(projectTasksMap.values());
        var fillingWbs = new FillingWbs();
        fillingWbs.fillWbs(service);
    }

    @Override
    public void fillWbs(ChangeHierarchyTransactionalService service) {

        var fillingWbs = new FillingWbs();
        fillingWbs.fillWbs(service);
    }

    private class FillingWbs {
        private final List<?> cycledPTIds;

        FillingWbs() {
            if (projectTasks.isEmpty()) {
                cycledPTIds = new ArrayList<>(0);
                return;
            }
            cycledPTIds = projectTasks.stream().map(ProjectTask::getId).toList();
        }

        private void fillWbs(ProjectTaskService service) {
            if (cycledPTIds.isEmpty()) return;
            Map<?, ProjectTask> projectTasksMap = service.getProjectTasksByIdWithFilledWbs(cycledPTIds);
            fillWbsLocal(projectTasksMap);
        }

        private void fillWbs(ChangeHierarchyTransactionalService service) {
            if (cycledPTIds.isEmpty()) return;
            Map<?, ProjectTask> projectTasksMap = service.getProjectTasksByIdWithFilledWbs(cycledPTIds);
            fillWbsLocal(projectTasksMap);
        }

        private void fillWbsLocal(Map<?, ProjectTask> projectTasksMap) {
            projectTasks.clear();
            projectTasks.addAll(projectTasksMap.values());
        }

    }
}
