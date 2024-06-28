package com.pmvaadin.project.data;

import com.pmvaadin.common.ChangedTableData;
import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.links.entities.Link;
import com.pmvaadin.project.resources.entity.TaskResource;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectTaskData {

    ProjectTask getProjectTask();
    void setProjectTask(ProjectTask projectTask);
    ChangedTableData<? extends Link> getLinksChangedTableData();
    void setLinksChangedTableData(ChangedTableData<? extends Link> linksChanges);
    List<Link> getLinks();
    void setLinks(List<Link> links);

    LocalDateTime getProjectStartDate();

    Calendar getCalendar();
    void setCalendar(Calendar calendar);

    TimeUnit getTimeUnit();
    void setTimeUnit(TimeUnit timeUnit);

    Link getLinkSample();
    void setLinkSample(Link link);

    List<TaskResource> getTaskResources();
    void setTaskResources(List<TaskResource> taskResources);

    Link getLinkInstance();
    TaskResource getTaskResourceInstance();

    List<Link> getSuccessors();

}
