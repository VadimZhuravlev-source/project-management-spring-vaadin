package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTaskOrderedHierarchy;

public interface TreeProjectTasks<V extends ProjectTaskOrderedHierarchy> {

    void fillWbs(SimpleTree<V> rootItemSimpleTree);

}
