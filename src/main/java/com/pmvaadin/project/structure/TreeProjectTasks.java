package com.pmvaadin.project.structure;

import com.pmvaadin.common.tree.TreeItem;
import com.pmvaadin.project.tasks.entity.ProjectTask;

import java.util.List;

public interface TreeProjectTasks {

    void fillWbs();
    void populateTreeByList(List<ProjectTask> list);
    void validateTree();
    TreeItem<ProjectTask> getRootItem();
    List<ProjectTask> recalculateThePropertiesOfTheWholeProject();
    List<ProjectTask> recalculateLevelOrderForProjectTasks(List<ProjectTask> projectTasks);

}
