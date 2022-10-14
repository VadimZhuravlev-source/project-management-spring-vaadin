package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.Links.Link;
import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;

import java.util.List;

public interface LinkService {

    List<Link> getAllLinks();
    List<Link> getProjectTaskLinks(ProjectTask projectTask);

}
