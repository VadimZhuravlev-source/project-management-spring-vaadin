package com.pmvaadin.project.data;

import com.pmvaadin.common.ChangedTableData;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.links.entities.Link;
import com.pmvaadin.project.links.entities.LinkImpl;
import com.pmvaadin.project.resources.labor.entity.TaskResource;
import com.pmvaadin.project.resources.labor.entity.TaskResourceImpl;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
//@AllArgsConstructor
public class ProjectTaskDataImpl implements ProjectTaskData {

    private ProjectTask projectTask;

    // Links
    private ChangedTableData<? extends Link> linksChangedTableData;
    private List<Link> links;

    private LocalDateTime projectStartDate;

    private Calendar calendar;

    private TimeUnit timeUnit;

    private Link linkSample;

    private List<TaskResource> taskResources;
    private List<Link> successors = new ArrayList<>(0);

    public ProjectTaskDataImpl(ProjectTask projectTask, List<Link> links, LocalDateTime projectStartDate,
                               Calendar calendar, TimeUnit timeUnit, Link linkSample) {
        this.projectTask = projectTask;
        this.links = links;
        this.projectStartDate = projectStartDate;
        this.calendar = calendar;
        this.timeUnit = timeUnit;
        this.linkSample = linkSample;
    }

    @Override
    public Link getLinkInstance() {
        return new LinkImpl();
    }

    @Override
    public TaskResource getTaskResourceInstance() {
        return new TaskResourceImpl();
    }

}
