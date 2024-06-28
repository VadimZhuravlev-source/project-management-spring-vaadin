package com.pmvaadin.project.structure;

import com.pmvaadin.common.tree.TreeItem;

import java.util.List;
import java.util.Set;

public interface Unloopable {

    <T> Set<T> detectCycle(List<? extends TreeItem<T>> treeItems);

}
