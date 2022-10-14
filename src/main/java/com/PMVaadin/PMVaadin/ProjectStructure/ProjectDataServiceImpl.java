package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;
import com.PMVaadin.PMVaadin.Services.LinkService;
import com.PMVaadin.PMVaadin.Services.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProjectDataServiceImpl implements ProjectDataService {

    private ProjectTaskService projectTaskService;
    private LinkService linkService;

    @Autowired
    public void setProjectTaskService(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Autowired
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

    @Override
    public ProjectData getProjectData() {

        return new ProjectDataImpl(projectTaskService.getTreeProjectTasks(), linkService.getAllLinks());

    }

    @Override
    public ProjectTask saveTask(ProjectTask projectTask) {

        return projectTaskService.saveTask(projectTask);

    }

    @Override
    public void deleteTasks(List<? extends ProjectTask> projectTasks) {
        projectTaskService.deleteTasks(projectTasks);
    }

    @Override
    public void setNewParentOfTheTasks(Set<? extends ProjectTask> projectTasks, ProjectTask parent) {
        projectTaskService.setNewParentOfTheTasks(projectTasks, parent);
    }
    @Override
    public List<ProjectTask> swapTasks(Map<ProjectTask, ProjectTask> swappedTasks) {
        return projectTaskService.swapTasks(swappedTasks);
    }

}
