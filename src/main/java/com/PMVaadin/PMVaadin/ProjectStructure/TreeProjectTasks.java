package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;
import com.PMVaadin.PMVaadin.CommonObjects.Tree.TreeItem;

import java.util.List;

public interface TreeProjectTasks {

    void fillWbs();
    void populateTreeByList(List<ProjectTask> list);
    void validateTree();
    TreeItem<ProjectTask> getRootItem();
    List<ProjectTask> recalculateThePropertiesOfTheWholeProject();
    List<ProjectTask> recalculateLevelOrderForProjectTasks(List<ProjectTask> projectTasks);

}
