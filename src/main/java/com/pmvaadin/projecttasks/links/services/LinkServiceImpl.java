package com.pmvaadin.projecttasks.links.services;

import com.pmvaadin.commonobjects.ChangedTableData;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.repositories.LinkRepository;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class LinkServiceImpl implements LinkService {

    private LinkRepository linkRepository;
    private ProjectTaskService projectTaskService;

    @Autowired
    public void setLinkRepository(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Autowired
    public void setProjectTaskService(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Override
    public List<? extends Link> getAllLinks() {
        return linkRepository.findAll();
    }

    @Override
    public List<? extends Link> getLinks(ProjectTask projectTask) {
        return linkRepository.findAllByProjectTaskId(projectTask.getId());
    }

    @Override
    public boolean validate(ChangedTableData<? extends Link> linksChangedTableData, ProjectTask projectTask) {
        return true;
    }

    @Override
    public List<? extends Link> save(List<? extends Link> links) {

        if (links.size() == 0) return new ArrayList<>();

        return linkRepository.saveAll(new ArrayList<>(links));
    }

    @Override
    public void delete(List<? extends Link> links) {

        if (links.size() == 0) return;

        linkRepository.deleteAllById(links.stream().map(Link::getId).distinct().toList());

    }

    @Override
    public void fillSort(List<? extends Link> links, ProjectTask projectTask) {

        if (links.size() == 0) return;

        Integer sort = linkRepository.findMaxSortOnProjectTask(projectTask.getId());
        int sortInt = 0;
        if (!Objects.isNull(sort)) sortInt = sort;
        for (Link link:links) {
            link.setSort(++sortInt);
        }
    }

    @Override
    public List<? extends Link> getLinksWithProjectTaskRepresentation(ProjectTask projectTask) {
        List<? extends Link> links = getLinks(projectTask);
        fillRepresentation(links);
        return links;
    }

    private void fillRepresentation(List<? extends Link> links) {

        if (links.size() == 0) return;

        List<Integer> projectTasksIds = links.stream().map(Link::getLinkedProjectTaskId).toList();
        Map<Integer, ProjectTask> projectTaskMap = projectTaskService.getProjectTasksWithWbs(projectTasksIds);
        links.forEach(link -> {
            ProjectTask projectTask = projectTaskMap.getOrDefault(link.getLinkedProjectTaskId(), null);
            if (projectTask == null) return;
            link.setRepresentation(projectTask.getLinkPresentation());
            link.setLinkedProjectTask(projectTask);
        });
    }

}
