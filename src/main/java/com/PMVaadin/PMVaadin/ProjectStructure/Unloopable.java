package com.PMVaadin.PMVaadin.ProjectStructure;

import java.util.List;

public interface Unloopable {

    void detectCycle(List<? extends TreeItem<?>> treeItems);

}
