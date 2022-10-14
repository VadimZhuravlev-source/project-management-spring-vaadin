package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Tree.TreeItem;

import java.util.List;

public interface Unloopable {

    void detectCycle(List<? extends TreeItem<?>> treeItems);

}
