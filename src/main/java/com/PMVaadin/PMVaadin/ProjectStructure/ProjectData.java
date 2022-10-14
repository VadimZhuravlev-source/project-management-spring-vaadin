package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.Links.Link;
import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;
import com.PMVaadin.PMVaadin.Tree.TreeItem;

import java.util.List;

public interface ProjectData {

    List<ProjectTask> getProjectTasks();
    List<Link> getLinks();

}
