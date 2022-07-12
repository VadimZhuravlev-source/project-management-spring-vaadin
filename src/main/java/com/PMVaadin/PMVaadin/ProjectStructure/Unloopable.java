package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;

import java.util.List;

public interface Unloopable {

    void detectCycle(List<? extends TreeItem> treeItems) throws Exception;

}
