package com.PMVaadin.PMVaadin.ProjectTasks.Links.Services;

import com.PMVaadin.PMVaadin.ProjectTasks.Links.Entities.Link;
import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;

import java.util.List;

public interface LinkService {

    List<Link> getAllLinks();
    List<Link> getLinks(ProjectTask projectTask);

}
