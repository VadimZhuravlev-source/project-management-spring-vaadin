package com.PMVaadin.PMVaadin.CommonObjects.Tree;

import java.util.List;
import java.util.function.Function;

public interface Tree<V> {

    TreeItem<V> getRootItem();
    List<TreeItem<V>> getTreeItems();
    <T> List<TreeItem<V>> getTreeItems(List<V> hierarchyElements,
                                       Function<V, T> getId,
                                       Function<V, T> getParentId);

}
