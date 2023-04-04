package com.pmvaadin.projecttasks.services;

import com.pmvaadin.commonobjects.ChangedTableData;
import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.data.ProjectTaskDataImpl;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.services.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProjectTaskDataServiceImpl implements ProjectTaskDataService{

    private ProjectTaskService projectTaskService;
    private LinkService linkService;

    @Autowired
    void setProjectTaskService(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Autowired
    void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

    @Override
    @Transactional
    public ProjectTaskData save(ProjectTaskData projectTaskData) {

        // validation
        boolean validationPass = projectTaskService.validate(projectTaskData.getProjectTask());
        if (!validationPass) return projectTaskData;

        fillLinksByChanges(projectTaskData);

        validationPass = linkService.validate(projectTaskData);
        if (!validationPass) return projectTaskData;

        return saveData(projectTaskData);

    }

    @Override
    @Transactional(readOnly = true)
    public ProjectTaskData read(ProjectTask projectTask) {

        if (projectTask.isNew()) return null;

        ProjectTask syncedProjectTask = projectTaskService.sync(projectTask);
        List<Link> links = linkService.getLinksWithProjectTaskRepresentation(syncedProjectTask);

        return new ProjectTaskDataImpl(syncedProjectTask, null, links);

    }

    private void fillLinksByChanges(ProjectTaskData projectTaskData) {

        if (projectTaskData.getLinksChangedTableData() == null && projectTaskData.getLinks() != null) return;

        linkService.fillLinksByChanges(projectTaskData);

    }

    private ProjectTaskData saveData(ProjectTaskData projectTaskData) {

        ProjectTask projectTask = projectTaskData.getProjectTask();
        boolean isNew = projectTask.isNew();
        if (isNew) {
            projectTask = projectTaskService.save(projectTask, false, false);
        }

        fillMainFields(projectTaskData);

        //linkService.fillSort(newLinks, projectTask);
        ChangedTableData<? extends Link> changedTableData = projectTaskData.getLinksChangedTableData();
        List<? extends Link> newLinks = changedTableData.getNewItems();
        List<? extends Link> deletedLinks = changedTableData.getDeletedItems();
        boolean increaseCheckSum = newLinks.size() > 0 || deletedLinks.size() > 0;
        linkService.delete(deletedLinks);

        increaseCheckSum = increaseCheckSum || isChangedFields(projectTaskData);

        saveChanges(changedTableData);

        if (!isNew) {
            if (increaseCheckSum) {
                int checkSum = projectTask.getLinksCheckSum();
                projectTask.setLinksCheckSum(++checkSum);
            }
            projectTask = projectTaskService.save(projectTask, false, false);
        }

        List<Link> links = linkService.getLinksWithProjectTaskRepresentation(projectTask);
        projectTaskService.fillParent(projectTask);

        return new ProjectTaskDataImpl(projectTask, null, links);

    }

    private void fillMainFields(ProjectTaskData projectTaskData) {

        var links= projectTaskData.getLinks();
        var projectTask = projectTaskData.getProjectTask();

        if (links.size() == 0) return;

        int maxSort = links.stream().map(Link::getSort).filter(Objects::nonNull).max(Integer::compare).orElse(0);

        for (Link link:links) {
            if (link.getProjectTaskId() == null) link.setProjectTaskId(projectTask.getId());
            if (Objects.isNull(link.getSort())) link.setSort(++maxSort);
        }

    }

    private void saveChanges(ChangedTableData<? extends Link> changedTableData) {

        List<? extends Link> newLinks = changedTableData.getNewItems();
        List<? extends Link> changedLinks = changedTableData.getChangedItems();
        List<Link> savedLinks = new ArrayList<>(newLinks.size() + changedLinks.size());
        savedLinks.addAll(newLinks);
        // TODO to check an existence of the changed links
        savedLinks.addAll(changedLinks);
        linkService.save(savedLinks);

    }

    private boolean isChangedFields(ProjectTaskData projectTaskData) {

        List<? extends Link> changedLinks = projectTaskData.getLinksChangedTableData().getChangedItems();

        if (changedLinks.size() == 0) return false;

        return identifyChanges(changedLinks, projectTaskData.getLinks());

    }

    private boolean identifyChanges(List<? extends Link> changedLinks, List<? extends Link> links) {

        var oldValuesLinkedPTMap = changedLinks.stream().collect(Collectors.toMap(Link::getId, Link::getLinkedProjectTaskId));
        var oldValuesLinkTypeMap = changedLinks.stream().collect(Collectors.toMap(Link::getId, Link::getLinkType));

        List<Link> deletedLinks = new ArrayList<>();

        boolean anyMatch = links.stream().anyMatch(link -> {

            var linkedPTId = oldValuesLinkedPTMap.getOrDefault(link.getId(), null);
            boolean isEqual = Objects.equals(linkedPTId, link.getLinkedProjectTaskId());

            if (!isEqual) {
                return true;
            }

            var linkType = oldValuesLinkTypeMap.getOrDefault(link.getId(), null);

            isEqual = Objects.equals(linkType, link.getLinkType());

            if (!isEqual) {
                return true;
            }

            deletedLinks.add(link);

            return false;

        });

        if (deletedLinks.size() == 0) changedLinks.removeAll(deletedLinks);

        return anyMatch;

    }

}
