package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.ProjectTasks.Links.Entities.Link;
import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;

import java.util.List;

public class ProjectDataImpl implements ProjectData {

    private ProjectTask projectTask;

    private List<Link> links;

    public ProjectDataImpl(ProjectTask projectTask, List<Link> links) {

        this.projectTask = projectTask;
        this.links = links;

    }

    @Override
    public ProjectTask getProjectTask() {
        return projectTask;
    }

    @Override
    public List<Link> getLinks() {
        return links;
    }
}
