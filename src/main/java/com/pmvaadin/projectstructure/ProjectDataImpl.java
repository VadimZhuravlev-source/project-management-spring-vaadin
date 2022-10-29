package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;

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
