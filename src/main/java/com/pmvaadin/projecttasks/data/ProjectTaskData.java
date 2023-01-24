package com.pmvaadin.projecttasks.data;

import com.pmvaadin.commonobjects.ChangedTableData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.List;

public interface ProjectTaskData {

    ProjectTask getProjectTask();
    ChangedTableData<? extends Link> getLinksChangedTableData();
    List<Link> getLinks();

}
