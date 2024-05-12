package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.services.TreeHierarchyChangeService;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskTreeProvider extends ProjectHierarchicalDataProvider {

    private final List<String> chosenColumns;
    private final TreeGrid<ProjectTask> treeGrid;
    private Set<ProjectTask> selectedItems;
    private final HashMap<ProjectTask, List<ProjectTask>> tempTree = new HashMap<>();
    private boolean formTempTree;


    public TaskTreeProvider(TreeHierarchyChangeService hierarchyService, List<String> chosenColumns, TreeGrid<ProjectTask> treeGrid) {
        super(hierarchyService);
        this.chosenColumns = chosenColumns;
        this.treeGrid = treeGrid;
    }

    public void setSelectedItems(Set<ProjectTask> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public Map<ProjectTask, List<ProjectTask>> getTempTree() {
        return tempTree;
    }

    public void setFormTempTree(boolean formTempTree) {
        this.formTempTree = formTempTree;
    }

    @Override
    public int getChildCount(HierarchicalQuery<ProjectTask, String> query) {
        return super.getChildCount(query);
    }

    @Override
    public boolean hasChildren(ProjectTask item) {
        return super.hasChildren(item);
    }

    @Override
    public void refreshAll() {
        tempTree.clear();
        super.refreshAll();
    }

    @Override
    protected Stream<ProjectTask> fetchChildrenFromBackEnd(HierarchicalQuery<ProjectTask, String> query) {
        var parent = query.getParent();
        var children = super.hierarchyService.getChildren(parent, chosenColumns, super.filter);
        if (selectedItems != null && !selectedItems.isEmpty()) {
            var multiSelect = treeGrid.asMultiSelect();
            //var selectedItems = multiSelect.getSelectedItems();
            var selectedTreeItems = selectedItems.stream().filter(p -> {
                if (parent == null || parent.getId() == null)
                    return p.getParentId() == null;
                else if (p.getParentId() != null)
                    return parent.getId().equals(p.getParentId());
                else
                    return false;
            }).collect(Collectors.toSet());

            if (!selectedTreeItems.isEmpty()) {
                var newSelectedItems = new HashSet<>(selectedItems);
                newSelectedItems.removeAll(selectedTreeItems);
                var copyChildren = new HashSet<>(children);
                copyChildren.retainAll(selectedTreeItems);
                newSelectedItems.addAll(copyChildren);
                var currentSelectedItems = multiSelect.getSelectedItems();
                var newSet = new HashSet<>(currentSelectedItems);
                newSet.addAll(newSelectedItems);
                selectedItems = new HashSet<>(selectedItems);
                selectedItems.removeAll(newSet);
                multiSelect.setValue(newSet);
            }
        }

//        if (formTempTree)
            tempTree.put(parent, children);

        return children.stream();

    }

}
