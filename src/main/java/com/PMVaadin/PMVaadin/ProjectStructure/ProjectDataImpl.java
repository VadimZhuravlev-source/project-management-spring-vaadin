package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.Links.Link;
import com.PMVaadin.PMVaadin.Entities.ProjectTask.ProjectTask;
import com.PMVaadin.PMVaadin.Tree.TreeItem;

import java.util.List;

public class ProjectDataImpl implements ProjectData {

    private List<ProjectTask> projectTasks;

    private List<Link> links;

    public ProjectDataImpl(List<ProjectTask> projectTasks, List<Link> links) {

        this.projectTasks = projectTasks;
        this.links = links;

    }

    @Override
    public List<ProjectTask> getProjectTasks() {
        return projectTasks;
    }

    @Override
    public List<Link> getLinks() {
        return links;
    }
}
