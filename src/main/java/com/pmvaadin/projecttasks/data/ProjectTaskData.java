package com.pmvaadin.projecttasks.data;

import com.pmvaadin.commonobjects.ChangedTableData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.List;

public interface ProjectTaskData {

    ProjectTask getProjectTask();
    void setProjectTask(ProjectTask projectTask);
    ChangedTableData<? extends Link> getLinksChangedTableData();
    void setLinksChangedTableData(ChangedTableData<? extends Link> linksChangedTableData);

    List<? extends Link> getLinks();

    void setLinks(List<? extends Link> links);

}
