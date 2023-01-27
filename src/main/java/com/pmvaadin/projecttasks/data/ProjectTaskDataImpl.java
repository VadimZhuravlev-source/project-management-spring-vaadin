package com.pmvaadin.projecttasks.data;

import com.pmvaadin.commonobjects.ChangedTableData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProjectTaskDataImpl implements ProjectTaskData {

    private ProjectTask projectTask;

    // Links
    private ChangedTableData<? extends Link> linksChangedTableData;
    private List<Link> links;

}
