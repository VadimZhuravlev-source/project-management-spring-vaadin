package com.pmvaadin.projecttasks.links.services;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface LinkService {

    List<Link> getAllLinks();
    List<Link> getLinks(ProjectTask projectTask);

}
