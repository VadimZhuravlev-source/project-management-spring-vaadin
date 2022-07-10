package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTaskOrderedHierarchy;

import java.util.List;

public interface TreeProjectTasks<V extends ProjectTaskOrderedHierarchy> {

    void fillWbs();
    void populateTreeByList(List<V> list);
    ValidationsMessage validateTree();

}
