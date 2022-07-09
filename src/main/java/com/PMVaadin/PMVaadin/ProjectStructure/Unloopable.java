package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;
import com.PMVaadin.PMVaadin.Entities.ProjectTask;

import java.util.List;

public interface Unloopable<V extends HierarchyElement> {

    boolean detectCycle(List<SimpleTree<V>> treeItems);

}
