package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;

import java.util.*;
import java.util.function.Function;

public class TreeItem<V extends HierarchyElement> implements SimpleTree<V> {

    private V value;
    private List<SimpleTree<V>> children = new LinkedList<>();
    private SimpleTree parent;

    public TreeItem() {

    }

    public TreeItem(V value) {
        this.value = value;
    }


    @Override
    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public V getValue() {
        return value;
    }


    @Override
    public void setParent(SimpleTree parent) {
        this.parent = parent;
    }

    @Override
    public SimpleTree getParent() {
        return parent;
    }

    @Override
    public List<SimpleTree<V>> getChildren() {
        return children;
    }

    @Override
    public <T> SimpleTree getTreeByList(List<V> hierarchyElements,
                                                                                       Function<V, T> getId,
                                                                                       Function<V, T> getParentId){

        Map<T, TreeItem<V>> mappedIdAllElements = new HashMap<>();

        List<TreeItem<V>> treeItems = new ArrayList<>(hierarchyElements.size());
        for (V hierarchyElement: hierarchyElements) {
            TreeItem<V> treeItem = new TreeItem(hierarchyElement);
            T id = getId.apply(hierarchyElement);
            mappedIdAllElements.put(id, treeItem);
            treeItems.add(treeItem);
        }

        TreeItem<V> rootItem = nullTreeItem();
        for (TreeItem<V> treeItem : treeItems) {
            V projectTask = treeItem.getValue();
            T parentId = getParentId.apply(projectTask);
            TreeItem<V> parent = mappedIdAllElements.get(parentId);
            if (parent == null) {
                parent = rootItem;
            }
            if (parent == treeItem){
                continue;
            }
            parent.getChildren().add(treeItem);
            treeItem.setParent(parent);
        }

        return rootItem;

    }

    private TreeItem nullTreeItem() {
        return new TreeItem();
    }

}
