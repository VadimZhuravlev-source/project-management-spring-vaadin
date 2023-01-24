package com.pmvaadin.projectstructure;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.services.ProjectTaskService;

public class TestCase {

    private static final int countElementsOnLevel = 15;
    private static final int countLevels = 4;
    private static final String namePattern = "Test task %s";

    public static void createTestCase(ProjectTaskService projectTaskService) {

        // Root task
        ProjectTask projectTask = new ProjectTaskImpl();
        projectTask.setName("Test root task!");
        projectTask = projectTaskService.save(projectTask, true, false);

        String additionWbs = "1";
        createTasksOnLevelRecursively(projectTask.getId(), projectTaskService, 1, additionWbs);

    }

    private static void createTasksOnLevelRecursively(Integer parentId,
                                               ProjectTaskService projectTaskService, int currentLevel, String nameWbs) {

        if (currentLevel > countLevels) return;

        int newLevel = currentLevel + 1;
        for (int i = 1; i <= countElementsOnLevel; i++) {

            ProjectTask projectTask = new ProjectTaskImpl();
            String newNameWbs = nameWbs +  "." + i;
            projectTask.setName(String.format(namePattern, newNameWbs));
            projectTask.setParentId(parentId);
            projectTaskService.save(projectTask, true, false);

            createTasksOnLevelRecursively(projectTask.getId(), projectTaskService, newLevel, newNameWbs);

        }
    }

}
