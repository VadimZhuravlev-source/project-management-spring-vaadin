package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTaskOrderedHierarchy;

import java.util.ArrayList;
import java.util.List;

public class TreeProjectTasksImpl<V extends ProjectTaskOrderedHierarchy> implements TreeProjectTasks<V>{

    private TreeItem<V> rootItem = new SimpleTreeItem<>();
    private List<TreeItem<V>> treeItems = new ArrayList<>();
    private Validations<V> validations = new ValidationsImpl<>();

    @Override
    public void fillWbs() {

        fillWbsRecursively(rootItem.getChildren(), "");

    }

    @Override
    public void populateTreeByList(List<V> projectTasks) {

        TreeItemList<V> treeItemList = new TreeItemList<>();
        this.treeItems = treeItemList.getTreeItemList(projectTasks, V::getId, V::getParentId);
        this.rootItem = treeItemList.getRootItem();

    }

    @Override
    public ValidationsMessage validateTree() {

        boolean isDetectedCycle = validations.detectCycle(treeItems);
        if (isDetectedCycle)
            return new ValidationsMessageImpl(false, "Detect cycle in tree");

        boolean isQuantitiesEquals = validations.checkQuantitiesTreeItemInTree(rootItem, treeItems);
        if (!isQuantitiesEquals)
            return new ValidationsMessageImpl(false, "Quantities in rootTree and treeItems list aren't equals");

        return new ValidationsMessageImpl();

    };

    private void fillWbsRecursively(List<TreeItem<V>> children, String previousWbs) {

        for (TreeItem<V> child: children) {

            V hierarchyElement = child.getValue();
            String newWbs = previousWbs + hierarchyElement.getLevelOrder().toString();
            hierarchyElement.setWbs(newWbs);
            fillWbsRecursively(child.getChildren(), newWbs + ".");

        }

    }

}
