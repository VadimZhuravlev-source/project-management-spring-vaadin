package com.pmvaadin.projecttasks.data;

import com.pmvaadin.commonobjects.ChangedTableData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.terms.calendars.entity.Calendar;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectTaskData {

    ProjectTask getProjectTask();
    ChangedTableData<? extends Link> getLinksChangedTableData();
    void setLinksChangedTableData(ChangedTableData<? extends Link> linksChanges);
    List<Link> getLinks();
    void setLinks(List<Link> links);

    LocalDateTime getProjectStartDate();

    Calendar getCalendar();
    void setCalendar(Calendar calendar);

}
