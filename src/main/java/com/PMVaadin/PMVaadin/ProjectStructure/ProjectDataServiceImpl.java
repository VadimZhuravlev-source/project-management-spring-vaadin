package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.ProjectTasks.Links.Entities.Link;
import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;
import com.PMVaadin.PMVaadin.ProjectTasks.Links.Services.LinkService;
import com.PMVaadin.PMVaadin.ProjectTasks.Services.ProjectTaskService;
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

    public List<ProjectTask> getTreeProjectTasks() {
        return projectTaskService.getTreeProjectTasks();
    }

    @Override
    public ProjectData getProjectData(ProjectTask projectTask) {
        return new ProjectDataImpl(projectTaskService.refreshTask(projectTask),
                linkService.getLinks(projectTask));
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

    @Override
    public ProjectTask refreshTask(ProjectTask projectTask) {
        return projectTaskService.refreshTask(projectTask);
    }

    @Override
    public List<Link> getLinks(ProjectTask projectTask) {
        return linkService.getLinks(projectTask);
    }

}
