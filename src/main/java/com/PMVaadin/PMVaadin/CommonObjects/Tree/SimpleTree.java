package com.PMVaadin.PMVaadin.CommonObjects.Tree;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@NoArgsConstructor
public class SimpleTree<V> implements Tree<V> {

    private final TreeItem<V> rootItem = new SimpleTreeItem<>();
    private List<TreeItem<V>> treeItems;

    public <T> SimpleTree(List<V> hierarchyElements,
                          Function<V, T> getId,
                          Function<V, T> getParentId) {

        populateTree(hierarchyElements, getId, getParentId);

    }

    public TreeItem<V> getRootItem() {
        return rootItem;
    }

    public List<TreeItem<V>> getTreeItems() {

        if (treeItems == null) {
            return new ArrayList<>();
        }

        return treeItems;
    }

    public <T> List<TreeItem<V>> getTreeItems(List<V> hierarchyElements,
                                              Function<V, T> getId,
                                              Function<V, T> getParentId) {

        populateTree(hierarchyElements, getId, getParentId);
        return treeItems;

    }
    private <T> void populateTree(List<V> hierarchyElements,
                              Function<V, T> getId,
                              Function<V, T> getParentId) {

        Map<T, TreeItem<V>> mapIdTreeItem = new HashMap<>();

        List<TreeItem<V>> treeItems = new ArrayList<>(hierarchyElements.size());
        for (V hierarchyElement: hierarchyElements) {
            TreeItem<V> treeItem = new SimpleTreeItem<>(hierarchyElement);
            T id = getId.apply(hierarchyElement);
            mapIdTreeItem.put(id, treeItem);
            treeItems.add(treeItem);
        }

        for (TreeItem<V> treeItem : treeItems) {
            V hierarchyElement = treeItem.getValue();
            T parentId = getParentId.apply(hierarchyElement);
            TreeItem<V> parent = mapIdTreeItem.get(parentId);
            if (parent == null) {
                parent = rootItem;
            }
            if (parent == treeItem){ //ToDo throw exception
                continue;
            }
            parent.getChildren().add(treeItem);
            treeItem.setParent(parent);
        }

        this.treeItems = treeItems;

    }

}
