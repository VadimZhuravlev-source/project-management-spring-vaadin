package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.TreeHierarchyChangeService;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.shared.Registration;

import java.util.stream.Stream;

public class ProjectHierarchicalDataProvider extends AbstractBackEndHierarchicalDataProvider<ProjectTask, Void> {

    private TreeHierarchyChangeService hierarchyService;

    private int cacheChildrenCountUpperLevel;
    private boolean firstInitialization;

    public ProjectHierarchicalDataProvider(TreeHierarchyChangeService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    public void setFirstInitialization(boolean firstInitialization) {
        this.firstInitialization = firstInitialization;
    }

    @Override
    public int size(Query<ProjectTask, Void> query) {
        return super.size(query);
    }

    @Override
    public Stream<ProjectTask> fetch(Query<ProjectTask, Void> query) {
        return fetchChildrenFromBackEndPrivate((HierarchicalQuery<ProjectTask, Void>) query);
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
    public Stream<ProjectTask> fetchChildren(HierarchicalQuery<ProjectTask, Void> query) {
        return fetchChildrenFromBackEndPrivate(query);
    }

    @Override
    protected Stream<ProjectTask> fetchChildrenFromBackEnd(HierarchicalQuery<ProjectTask, Void> query) {

        return fetchChildrenFromBackEndPrivate(query);

    }

    private Stream<ProjectTask> fetchChildrenFromBackEndPrivate(HierarchicalQuery<ProjectTask, Void> query) {

        ProjectTask parent = query.getParent();
        TreeHierarchyChangeService.FetchedData fetchedData = hierarchyService.getFetchedData(parent);
        cacheChildrenCountUpperLevel = fetchedData.getChildrenCountOfUpperLevel();
        if (parent != null) parent.setChildrenCount(fetchedData.getChildren().size());

        return fetchedData.getChildren().stream();

    }

}
