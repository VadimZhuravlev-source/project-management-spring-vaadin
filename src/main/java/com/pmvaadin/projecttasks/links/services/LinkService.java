package com.pmvaadin.projecttasks.links.services;

import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;
import java.util.Map;

public interface LinkService {

    List<Link> getLinks(ProjectTask projectTask);

    boolean validate(ProjectTaskData projectTaskData);

    List<? extends Link> save(List<? extends Link> links);

    void delete(List<? extends Link> links);

    List<Link> getLinksWithProjectTaskRepresentation(ProjectTask projectTask);

    void fillLinksByChanges(ProjectTaskData projectTaskData);

    Map<Object, List<Object>> getPredecessorsIds(List<?> ids);

}
