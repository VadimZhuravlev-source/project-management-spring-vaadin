package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.ProjectTasks.Links.Entities.Link;
import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;

import java.util.List;

public interface ProjectData {

    ProjectTask getProjectTask();
    List<Link> getLinks();

}
