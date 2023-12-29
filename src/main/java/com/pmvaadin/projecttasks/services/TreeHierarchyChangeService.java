package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface TreeHierarchyChangeService {

    FetchedData getFetchedData(ProjectTask projectTask);
    int getChildrenCount(ProjectTask projectTask);

    List<ProjectTask> getChildren(ProjectTask projectTask, List<String> chosenColumns);

    interface FetchedData {

        int getChildrenCountOfUpperLevel();

        List<ProjectTask> getChildren();

    }

}
