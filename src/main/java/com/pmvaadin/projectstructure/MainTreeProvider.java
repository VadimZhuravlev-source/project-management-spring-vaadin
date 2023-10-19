package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.TreeHierarchyChangeService;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import java.util.List;
import java.util.stream.Stream;

public class MainTreeProvider extends ProjectHierarchicalDataProvider {

    private final List<String> chosenColumns;

    public MainTreeProvider(TreeHierarchyChangeService hierarchyService, List<String> chosenColumns) {
        super(hierarchyService);
        this.chosenColumns = chosenColumns;
    }

    @Override
    public int getChildCount(HierarchicalQuery<ProjectTask, Void> query) {
        return super.getChildCount(query);
    }

    @Override
    public boolean hasChildren(ProjectTask item) {
        return super.hasChildren(item);
    }

    @Override
    protected Stream<ProjectTask> fetchChildrenFromBackEnd(HierarchicalQuery<ProjectTask, Void> query) {
        ProjectTask parent = query.getParent();
        return super.hierarchyService.getChildren(parent, chosenColumns).stream();
    }
}
