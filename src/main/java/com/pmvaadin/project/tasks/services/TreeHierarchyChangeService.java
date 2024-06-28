package com.pmvaadin.project.tasks.services;

import com.pmvaadin.project.structure.Filter;
import com.pmvaadin.project.tasks.entity.ProjectTask;

import java.util.List;

public interface TreeHierarchyChangeService {

    FetchedData getFetchedData(ProjectTask projectTask, Filter filter);
    int getChildrenCount(ProjectTask projectTask, Filter filter);

    List<ProjectTask> getChildren(ProjectTask projectTask, List<String> chosenColumns, Filter filter);

    interface FetchedData {

        int getChildrenCountOfUpperLevel();

        List<ProjectTask> getChildren();

    }

}
