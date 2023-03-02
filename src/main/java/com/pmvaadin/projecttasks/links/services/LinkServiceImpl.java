package com.pmvaadin.projecttasks.links.services;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.dependencies.DependenciesSetImpl;
import com.pmvaadin.projecttasks.links.LinkValidation;
import com.pmvaadin.projecttasks.links.LinkValidationImpl;
import com.pmvaadin.projecttasks.links.LinkValidationMessage;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.repositories.LinkRepository;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.projecttasks.dependencies.DependenciesService;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LinkServiceImpl implements LinkService {

    private LinkRepository linkRepository;
    private ProjectTaskService projectTaskService;
    private DependenciesService dependenciesService;

    @Autowired
    public void setLinkRepository(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Autowired
    public void setProjectTaskService(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Autowired
    public void setEntityManagerService(DependenciesService dependenciesService){
        this.dependenciesService = dependenciesService;
    }

    @Override
    public List<Link> getLinks(ProjectTask projectTask) {
        return linkRepository.findAllByProjectTaskIdOrderBySortAsc(projectTask.getId());
    }

    @Override
    public void fillLinksByChanges(ProjectTaskData projectTaskData) {

        var linksChangedTableData = projectTaskData.getLinksChangedTableData();
        var projectTask = projectTaskData.getProjectTask();

        // get all project task links
        List<? extends Link> newLinks = linksChangedTableData.getNewItems();
        List<? extends Link> changedLinks = linksChangedTableData.getChangedItems();
        List<? extends Link> deletedLinks = linksChangedTableData.getDeletedItems();

        var changedLinkIds = new ArrayList<>(changedLinks.size() + deletedLinks.size());
        changedLinkIds.addAll(changedLinks.stream().map(Link::getId).toList());
        changedLinkIds.addAll(deletedLinks.stream().map(Link::getId).toList());
        //var deletedLinkIds = linksChangedTableData.getDeletedItems().stream().map(Link::getId).toList();

        List<Link> projectTaskLinks = new ArrayList<>();
        if (!projectTask.isNew())
            if (changedLinkIds.size() == 0)
                projectTaskLinks = linkRepository.findAllByProjectTaskIdOrderBySortAsc(projectTask.getId());
            else
                projectTaskLinks = linkRepository.findAllByProjectTaskIdAndIdNotInIds(projectTask.getId(), changedLinkIds);

        projectTaskLinks.addAll(newLinks);
        projectTaskLinks.addAll(changedLinks);

        if (projectTaskData.getLinks() == null) projectTaskData.setLinks(new ArrayList<>());
        projectTaskData.getLinks().addAll(projectTaskLinks);

    }

    @Override
    public boolean validate(ProjectTaskData projectTaskData) {

        LinkValidation linkValidation = new LinkValidationImpl();
        LinkValidationMessage respond = linkValidation.validate(projectTaskData.getLinks());

        if (!respond.isOk()) {
            return false;
        }

        DependenciesSet dependenciesSet = getAllDependencies(projectTaskData);

        if (dependenciesSet.isCycle()) {
            String message = dependenciesService.getCycleLinkMessage(dependenciesSet);
            throw new StandardError(message);
        }

        return respond.isOk();

    }

    @Override
    public List<? extends Link> save(List<? extends Link> links) {

        if (links.size() == 0) return new ArrayList<>();

        return linkRepository.saveAll(links);
    }

    @Override
    public void delete(List<? extends Link> links) {

        if (links.size() == 0) return;

        var ids = links.stream().map(Link::getId).distinct().filter(Objects::nonNull).toList();

        linkRepository.deleteAllById(ids);

    }

    @Override
    public List<Link> getLinksWithProjectTaskRepresentation(ProjectTask projectTask) {
        List<Link> links = getLinks(projectTask);
        fillRepresentation(links);
        return links;
    }

    private void fillRepresentation(List<? extends Link> links) {

        if (links.size() == 0) return;

        List<?> projectTasksIds = links.stream().map(Link::getLinkedProjectTaskId).toList();
        Map<?, ProjectTask> projectTaskMap = projectTaskService.getProjectTasksByIdWithFilledWbs(projectTasksIds);
        links.forEach(link -> {
            ProjectTask projectTask = projectTaskMap.getOrDefault(link.getLinkedProjectTaskId(), null);
            if (projectTask == null) return;
            link.setRepresentation(projectTask.getLinkPresentation());
            link.setLinkedProjectTask(projectTask);
        });
    }

    private DependenciesSet getAllDependencies(ProjectTaskData projectTaskData) {

        var links = projectTaskData.getLinks();

        var projectTask = projectTaskData.getProjectTask();
        if (projectTask.isNew() & Objects.isNull(projectTask.getParentId()) || links.size() == 0)
            return new DependenciesSetImpl();

        var projectTasksIds = links.stream().map(Link::getLinkedProjectTaskId).toList();
        var parentId = projectTask.getId();
        if (projectTask.isNew()) parentId = projectTask.getParentId();

        DependenciesSet dependenciesSet = dependenciesService.getAllDependencies(parentId, projectTasksIds);

        if (dependenciesSet.isCycle()) {
            dependenciesSet.fillWbs(projectTaskService);
        }

        return dependenciesSet;

    }

}
