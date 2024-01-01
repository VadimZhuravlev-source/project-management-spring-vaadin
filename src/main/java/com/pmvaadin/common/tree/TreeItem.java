package com.pmvaadin.common.tree;

import java.util.List;

public interface TreeItem<V> {

    void setValue(V value);
    V getValue();
    void setParent(TreeItem<V> parent);
    TreeItem<V> getParent();
    List<TreeItem<V>> getChildren();

}
