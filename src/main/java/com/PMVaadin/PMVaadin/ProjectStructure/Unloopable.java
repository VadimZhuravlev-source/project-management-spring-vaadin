package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.CommonObjects.Tree.TreeItem;

import java.util.List;

public interface Unloopable {

    void detectCycle(List<? extends TreeItem<?>> treeItems);

}
