package com.pmvaadin.projectstructure;

import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.List;

public interface TreeProjectTasks {

    void fillWbs();
    void populateTreeByList(List<ProjectTask> list);
    void validateTree();
    TreeItem<ProjectTask> getRootItem();
    List<ProjectTask> recalculateThePropertiesOfTheWholeProject();
    List<ProjectTask> recalculateLevelOrderForProjectTasks(List<ProjectTask> projectTasks);

}
