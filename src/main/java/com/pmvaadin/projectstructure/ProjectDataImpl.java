package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public class ProjectDataImpl implements ProjectData {

    private ProjectTask projectTask;

    private List<? extends Link> links;

    public ProjectDataImpl(ProjectTask projectTask, List<? extends Link> links) {

        this.projectTask = projectTask;
        this.links = links;

    }

    @Override
    public ProjectTask getProjectTask() {
        return projectTask;
    }

    @Override
    public List<? extends Link> getLinks() {
        return links;
    }
}
