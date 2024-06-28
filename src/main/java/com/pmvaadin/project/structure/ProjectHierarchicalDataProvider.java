package com.pmvaadin.project.structure;

import com.pmvaadin.project.tasks.entity.ProjectTask;
import com.pmvaadin.project.tasks.services.TreeHierarchyChangeService;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import java.util.stream.Stream;

public class ProjectHierarchicalDataProvider extends AbstractBackEndHierarchicalDataProvider<ProjectTask, String> {

    protected final TreeHierarchyChangeService hierarchyService;
    protected Filter filter = new FilterImpl("", false);
    protected boolean showOnlyProjects;

//    private int cacheChildrenCountUpperLevel;
//    private boolean receiveRootChildrenCount;
//
//    private boolean isRefresh;
//    private final List<ProjectTask> cacheChildren = new ArrayList<>(0);

    public ProjectHierarchicalDataProvider(TreeHierarchyChangeService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    @Override
    public int getChildCount(HierarchicalQuery<ProjectTask, String> query) {

        ProjectTask item = query.getParent();
        return hierarchyService.getChildrenCount(item, filter);
//        if (item == null) {
//            if (receiveRootChildrenCount) {
//                cacheChildrenCountUpperLevel = hierarchyService.getChildrenCount(null);
//                receiveRootChildrenCount = false;
//            }
//            return cacheChildrenCountUpperLevel;
//        }
//
//        return item.getChildrenCount();

    }

    @Override
    public boolean hasChildren(ProjectTask item) {

        return item.getAmountOfChildren() != 0;

//        if (isRefresh) return item.getChildrenCount() != 0;
//
//        if (cacheChildren.contains(item)) {
//            if (cacheChildren.get(cacheChildren.size() - 1).equals(item)) cacheChildren.clear();
//            return item.getChildrenCount() != 0;
//        }
//
//        fetchDataByParent(item);
//
//        return cacheChildren.size() != 0;

    }

    @Override
    protected Stream<ProjectTask> fetchChildrenFromBackEnd(HierarchicalQuery<ProjectTask, String> query) {

        ProjectTask parent = query.getParent();
        TreeHierarchyChangeService.FetchedData fetchedData = hierarchyService.getFetchedData(parent, filter);

        return fetchedData.getChildren().stream();

//        if (cacheChildren.isEmpty()) fetchChildrenByQuery(query);
//
//        return cacheChildren.stream();

    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        this.refreshAll();
    }

//    @Override
//    public void refreshAll() {
//        receiveRootChildrenCount = true;
//        isRefresh = true;
//        cacheChildren.clear();
//        super.refreshAll();
//        isRefresh = false;
//    }
//
//    @Override
//    public void refreshItem(ProjectTask item) {
//        receiveRootChildrenCount = true;
//        isRefresh = true;
//        cacheChildren.clear();
//        super.refreshItem(item);
//        isRefresh = false;
//    }
//
//    @Override
//    public void refreshItem(ProjectTask item, boolean refreshChildren) {
//        receiveRootChildrenCount = true;
//        isRefresh = true;
//        cacheChildren.clear();
//        super.refreshItem(item, refreshChildren);
//        isRefresh = false;
//    }
//
//    private void fetchChildrenByQuery(HierarchicalQuery<ProjectTask, Void> query) {
//
//        ProjectTask parent = query.getParent();
//
//        fetchDataByParent(parent);
//        if (parent != null) parent.setChildrenCount(cacheChildren.size());
//
//    }
//
//    private void fetchDataByParent(ProjectTask parent) {
//
//        TreeHierarchyChangeService.FetchedData fetchedData = hierarchyService.getFetchedData(parent);
//        cacheChildrenCountUpperLevel = fetchedData.getChildrenCountOfUpperLevel();
//        cacheChildren.clear();
//        cacheChildren.addAll(fetchedData.getChildren());
//
//    }

}
