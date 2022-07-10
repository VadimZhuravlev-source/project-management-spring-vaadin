package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;

import java.util.List;

public interface Unloopable<V extends HierarchyElement> {

    boolean detectCycle(List<TreeItem<V>> treeItems);

}
