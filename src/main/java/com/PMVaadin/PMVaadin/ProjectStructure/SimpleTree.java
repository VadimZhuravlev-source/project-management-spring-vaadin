package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;

import java.util.List;
import java.util.function.Function;

public interface SimpleTree<V extends HierarchyElement> {

    void setValue(V value);
    V getValue();
    void setParent(SimpleTree parent);
    SimpleTree getParent();
    List<SimpleTree<V>> getChildren();
    <T> SimpleTree getTreeByList(List<V> hierarchyElements,
                                                      Function<V, T> getId,
                                                      Function<V, T> getParentId);

}
