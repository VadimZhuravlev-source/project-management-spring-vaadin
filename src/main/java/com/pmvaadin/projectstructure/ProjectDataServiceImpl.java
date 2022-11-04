package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.services.LinkService;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
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
        return new ProjectDataImpl(projectTaskService.sync(projectTask),
                linkService.getLinks(projectTask));
    }

    @Override
    public ProjectTask saveTask(ProjectTask projectTask) {

        return projectTaskService.save(projectTask, true, false);

    }

    @Override
    public void deleteTasks(List<? extends ProjectTask> projectTasks) {
        projectTaskService.delete(projectTasks);
    }

    @Override
    public void setNewParentOfTheTasks(Set<? extends ProjectTask> projectTasks, ProjectTask parent) {
        projectTaskService.changeParent(projectTasks, parent);
    }
    @Override
    public List<? extends ProjectTask> swapTasks(Map<? extends ProjectTask, ? extends ProjectTask> swappedTasks) {
        return projectTaskService.swap(swappedTasks);
    }

}
