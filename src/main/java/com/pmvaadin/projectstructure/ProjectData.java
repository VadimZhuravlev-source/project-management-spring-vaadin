package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface ProjectData {

    ProjectTask getProjectTask();
    List<? extends Link> getLinks();

}
