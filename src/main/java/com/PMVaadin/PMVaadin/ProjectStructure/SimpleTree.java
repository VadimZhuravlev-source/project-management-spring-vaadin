package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;

import java.util.List;
import java.util.function.Function;

public interface SimpleTree<V extends HierarchyElement> {

    void setValue(V value);
    V getValue();
    void setParent(SimpleTree<V> parent);
    SimpleTree<V> getParent();
    List<SimpleTree<V>> getChildren();
    SimpleTree<V> getRootItem();

    <T> List<SimpleTree<V>> getTreeItemList(List<V> hierarchyElements,
                                   Function<V, T> getId,
                                   Function<V, T> getParentId);

}
