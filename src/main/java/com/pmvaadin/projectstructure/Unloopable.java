package com.pmvaadin.projectstructure;

import com.pmvaadin.commonobjects.tree.TreeItem;

import java.util.List;

public interface Unloopable {

    void detectCycle(List<? extends TreeItem<?>> treeItems);

}
