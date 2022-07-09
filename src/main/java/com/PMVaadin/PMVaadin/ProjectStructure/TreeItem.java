package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;

import java.util.*;
import java.util.function.Function;

public class TreeItem<V extends HierarchyElement> implements SimpleTree<V> {

    private V value;
    private List<SimpleTree<V>> children = new LinkedList<>();
    private SimpleTree<V> parent;
    private SimpleTree<V> rootItem;

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
    public void setParent(SimpleTree<V> parent) {
        this.parent = parent;
    }

    @Override
    public SimpleTree<V> getParent() {
        return parent;
    }

    @Override
    public List<SimpleTree<V>> getChildren() {
        return children;
    }

    @Override
    public SimpleTree getRootItem() {
        return rootItem;
    }


    @Override
    public <T> List<SimpleTree<V>> getTreeItemList(List<V> hierarchyElements,
                                          Function<V, T> getId,
                                          Function<V, T> getParentId){

        Map<T, SimpleTree<V>> mappedIdAllElements = new HashMap<>();

        List<SimpleTree<V>> treeItems = new ArrayList<>(hierarchyElements.size());
        for (V hierarchyElement: hierarchyElements) {
            SimpleTree<V> treeItem = new TreeItem(hierarchyElement);
            T id = getId.apply(hierarchyElement);
            mappedIdAllElements.put(id, treeItem);
            treeItems.add(treeItem);
        }

        rootItem = nullTreeItem();
        for (SimpleTree<V> treeItem : treeItems) {
            V projectTask = treeItem.getValue();
            T parentId = getParentId.apply(projectTask);
            SimpleTree<V> parent = mappedIdAllElements.get(parentId);
            if (parent == null) {
                parent = rootItem;
            }
            if (parent == treeItem){ //ToDo throw exception
                continue;
            }
            parent.getChildren().add(treeItem);
            treeItem.setParent(parent);
        }

        return treeItems;

    }

    private TreeItem nullTreeItem() {
        return new TreeItem();
    }

}
