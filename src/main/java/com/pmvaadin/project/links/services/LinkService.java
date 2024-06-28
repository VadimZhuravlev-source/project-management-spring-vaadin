package com.pmvaadin.project.links.services;

import com.pmvaadin.project.links.entities.Link;
import com.pmvaadin.project.data.ProjectTaskData;
import com.pmvaadin.project.tasks.entity.ProjectTask;

import java.util.List;
import java.util.Map;

public interface LinkService {

    List<Link> getLinks(ProjectTask projectTask);

    boolean validate(ProjectTaskData projectTaskData);

    List<? extends Link> save(List<? extends Link> links);

    void delete(List<? extends Link> links);

    List<Link> getLinksAndSuccessorsWithProjectTaskRepresentation(ProjectTask projectTask);

    void fillLinksByChanges(ProjectTaskData projectTaskData);

    Map<Object, List<Object>> getPredecessorsIds(List<?> ids);

}
