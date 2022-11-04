package com.pmvaadin.projecttasks.links.services;

import com.pmvaadin.commonobjects.ChangedTableData;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface LinkService {

    List<? extends Link> getAllLinks();
    List<? extends Link> getLinks(ProjectTask projectTask);

    boolean validate(ChangedTableData<? extends Link> linksChangedTableData, ProjectTask projectTask);

    List<? extends Link> save(List<? extends Link> links);

    void delete(List<? extends Link> links);

    void fillSort(List<? extends Link> links, ProjectTask projectTask);

    List<? extends Link> getLinksWithProjectTaskRepresentation(ProjectTask projectTask);

}
