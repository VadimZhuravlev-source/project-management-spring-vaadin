package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;

import java.util.List;

public interface TreeProjectTasks {

    void fillWbs();
    void populateTreeByList(List<ProjectTask> list);
    void validateTree() throws Exception;
    TreeItem<ProjectTask> getRootItem();
    List<ProjectTask> recalculateThePropertiesOfTheWholeProject();
    List<ProjectTask> recalculateLevelOrderForProjectTasks(List<ProjectTask> projectTasks);

}
