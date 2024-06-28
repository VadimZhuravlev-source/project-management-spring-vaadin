package com.pmvaadin.project.links.services;

import com.pmvaadin.project.links.LinkValidation;
import com.pmvaadin.project.links.entities.Link;
import com.pmvaadin.project.links.repositories.LinkRepository;
import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.project.data.ProjectTaskData;
import com.pmvaadin.project.dependencies.DependenciesSet;
import com.pmvaadin.project.dependencies.DependenciesSetImpl;
import com.pmvaadin.project.links.LinkValidationImpl;
import com.pmvaadin.project.links.LinkValidationMessage;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.dependencies.DependenciesService;
import com.pmvaadin.project.tasks.services.ProjectTaskService;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LinkServiceImpl implements LinkService {

    private LinkRepository linkRepository;
    private ProjectTaskService projectTaskService;
    private DependenciesService dependenciesService;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setLinkRepository(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Autowired
    public void setProjectTaskService(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Autowired
    public void setDependenciesService(DependenciesService dependenciesService){
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
    public List<Link> getLinksAndSuccessorsWithProjectTaskRepresentation(ProjectTask projectTask) {
        List<Link> links = getLinksWithSuccessors(projectTask);
        fillRepresentation(links, projectTask);
        return links;
    }

    @Override
    public Map<Object, List<Object>> getPredecessorsIds(List<?> ids) {

        var queryText = """
                SELECT
                	project_task,
                	linked_project_task
                FROM
                	links
                WHERE
                	links.project_task IN(:ids) --= ANY('{2,3,4,5,6}')
                	AND links.linked_project_task IN(:ids) --= ANY('{2,3,4,5,6}')
                """;

        var query = entityManager.createNativeQuery(queryText);
        query.setParameter("ids", ids);

        var result = (List<Object[]>) query.getResultList();

        var map = new HashMap<Object, List<Object>>();

        result.forEach(o -> {
            var projectTaskId = o[0];
            var linkedPTId = o[1];
            var linkedTasks = map.getOrDefault(projectTaskId, new ArrayList<>());
            linkedTasks.add(linkedPTId);
            map.put(projectTaskId, linkedTasks);
        });

        return map;

    }

    private List<Link> getLinksWithSuccessors(ProjectTask projectTask) {
        return linkRepository.findAllWithSuccessorsByProjectTaskId(projectTask.getId());
    }

    private void fillRepresentation(List<? extends Link> links, ProjectTask linksOwner) {

        if (links.isEmpty()) return;
        var ids = links.stream().filter(link -> link.getLinkedProjectTaskId().equals(linksOwner.getId()))
                .map(Link::getProjectTaskId)
                .collect(Collectors.toList());
        var projectTasksIds = links.stream().filter(link -> link.getProjectTaskId().equals(linksOwner.getId())).map(Link::getLinkedProjectTaskId).toList();
        ids.addAll(projectTasksIds);
        Map<?, ProjectTask> projectTaskMap = projectTaskService.getProjectTasksByIdWithFilledWbs(ids);
        links.forEach(link -> {
            ProjectTask projectTask = projectTaskMap.getOrDefault(link.getLinkedProjectTaskId(), null);
            if (projectTask == null || link.getLinkedProjectTaskId().equals(linksOwner.getId())) {
                projectTask = projectTaskMap.get(link.getProjectTaskId());
                if (projectTask == null)
                    return;
                link.setRepresentation(projectTask.getName());
                link.setWbs(projectTask.getWbs());
                return;
            }
            link.setRepresentation(projectTask.getName());
            link.setWbs(projectTask.getWbs());
            link.setLinkedProjectTask(projectTask);
            var timeUnit = link.getTimeUnit();
            if (timeUnit instanceof TimeUnit)
                link.setLagRepresentation(((TimeUnit) timeUnit).getDurationRepresentation(link.getLag()));

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
