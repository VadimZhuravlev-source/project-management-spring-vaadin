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
import java.util.Map;
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

        if (!validate(projectTaskData)) return null;
        return saveData(projectTaskData);

    }

    @Override
    @Transactional(readOnly = true)
    public ProjectTaskData read(ProjectTask projectTask) {
        if (projectTask.isNew()) return null;
        ProjectTask refreshedProjectTask = projectTaskService.sync(projectTask);
        List<? extends Link> links = linkService.getLinksWithProjectTaskRepresentation(refreshedProjectTask);
        return new ProjectTaskDataImpl(refreshedProjectTask, null, links);
    }

    private boolean validate(ProjectTaskData projectTaskData) {
        boolean validationPass = projectTaskService.validate(projectTaskData.getProjectTask());
        if (!validationPass) return false;
        validationPass = linkService.validate(projectTaskData.getLinksChangedTableData(), projectTaskData.getProjectTask());
        if (!validationPass) return false;
        return true;
    }

    private ProjectTaskData saveData(ProjectTaskData projectTaskData) {

        ProjectTask projectTask = projectTaskData.getProjectTask();
        boolean isNew = projectTask.isNew();
        if (isNew) {
            projectTask = projectTaskService.save(projectTask, false, false);
        }

        ChangedTableData<? extends Link> changedTableData = projectTaskData.getLinksChangedTableData();
        List<? extends Link> newLinks = changedTableData.getNewItems();
        linkService.fillSort(newLinks, projectTask);
        boolean increaseCheckSum = newLinks.size() > 0;
        var idProjectTask = projectTask.getId();
        newLinks.forEach(link -> link.setProjectTaskId(idProjectTask));

        List<? extends Link> changedLinks = changedTableData.getChangedItems();

        boolean checkChangedLinks = !isNew && !increaseCheckSum;
        final Map<Integer, Integer> changedLinksMap;
        if (checkChangedLinks) {
            changedLinksMap = changedLinks.stream().collect(Collectors.toMap(Link::getId, Link::getVersion));
        } else {
            changedLinksMap = null;
        }
        List<Link> savedLinks = new ArrayList<>(newLinks.size() + changedLinks.size());
        savedLinks.addAll(newLinks);
        savedLinks.addAll(changedLinks);
        List<? extends Link> savedLinks1 = linkService.save(savedLinks);

        // check
        if (checkChangedLinks) {
            increaseCheckSum = savedLinks1.stream().anyMatch(link -> {
                Integer version = changedLinksMap.getOrDefault(link.getId(), null);
                return version != null && !version.equals(link.getVersion());
            });
        }

        List<? extends Link> deletedLinks = changedTableData.getDeletedItems();
        increaseCheckSum = increaseCheckSum || deletedLinks.size() > 0;
        linkService.delete(deletedLinks);

        if (!isNew) {
            if (increaseCheckSum) {
                int checkSum = projectTask.getLinksCheckSum();
                projectTask.setLinksCheckSum(++checkSum);
            }
            projectTask = projectTaskService.save(projectTask, false, false);
        }

        List<? extends Link> links = linkService.getLinksWithProjectTaskRepresentation(projectTask);

        return new ProjectTaskDataImpl(projectTask, null, links);

    }

}
