package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.TreeHierarchyChangeService;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import java.util.stream.Stream;

public class ProjectHierarchicalDataProvider extends AbstractBackEndHierarchicalDataProvider<ProjectTask, Void> {

    private TreeHierarchyChangeService hierarchyService;

    private int cacheChildrenCountUpperLevel;
    private boolean firstInitialization = true;

    public ProjectHierarchicalDataProvider(TreeHierarchyChangeService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    @Override
    public int getChildCount(HierarchicalQuery<ProjectTask, Void> query) {

        ProjectTask item = query.getParent();
        if (item == null) {
            if (firstInitialization) {
                cacheChildrenCountUpperLevel = hierarchyService.getChildrenCount(null);
                firstInitialization = false;
            }
            return cacheChildrenCountUpperLevel;
        }

        return item.getChildrenCount();

    }

    @Override
    public boolean hasChildren(ProjectTask item) {
        return item.getChildrenCount() != 0;
    }

    @Override
    protected Stream<ProjectTask> fetchChildrenFromBackEnd(HierarchicalQuery<ProjectTask, Void> query) {

        TreeHierarchyChangeService.FetchedData fetchedData = hierarchyService.getFetchedData(query.getParent());
        cacheChildrenCountUpperLevel = fetchedData.getChildrenCountOfUpperLevel();
        return fetchedData.getChildren().stream();

    }

}
