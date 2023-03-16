package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface TreeHierarchyChangeService {

    FetchedData getFetchedData(ProjectTask projectTask);
    int getChildrenCount(ProjectTask projectTask);

    interface FetchedData {

        int getChildrenCountOfUpperLevel();

        List<ProjectTask> getChildren();

    }

}
