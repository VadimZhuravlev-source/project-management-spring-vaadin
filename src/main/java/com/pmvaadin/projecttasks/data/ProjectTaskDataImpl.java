package com.pmvaadin.projecttasks.data;

import com.pmvaadin.commonobjects.ChangedTableData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProjectTaskDataImpl implements ProjectTaskData {

    private final ProjectTask projectTask;

    // Links
    private final ChangedTableData<? extends Link> linksChangedTableData;
    private final List<Link> links;

}
