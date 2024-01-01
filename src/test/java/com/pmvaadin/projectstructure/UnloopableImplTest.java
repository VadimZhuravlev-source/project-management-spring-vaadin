package com.pmvaadin.projectstructure;

import com.pmvaadin.common.tree.SimpleTree;
import com.pmvaadin.common.tree.Tree;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UnloopableImplTest {

    private final List<ProjectTask> projectTasksWithoutLooping = new ArrayList<>();
    private final List<ProjectTask> projectTasksWithLooping = new ArrayList<>();
    private final List<ProjectTask> cycle = new ArrayList<>();

    @BeforeEach
    void init() {

        ProjectTask projectTask1 = new ProjectTaskImpl();
        projectTask1.setId(1);
        projectTask1.setLevelOrder(1);
        projectTask1.setName("1");
        ProjectTask projectTask2 = new ProjectTaskImpl();
        projectTask2.setId(2);
        projectTask2.setLevelOrder(2);
        projectTask2.setName("2");
        ProjectTask projectTask11 = new ProjectTaskImpl();
        projectTask11.setId(3);
        projectTask11.setLevelOrder(1);
        projectTask11.setName("1.1");
        projectTask11.setParentId(1);
        ProjectTask projectTask21 = new ProjectTaskImpl();
        projectTask21.setId(5);
        projectTask21.setLevelOrder(1);
        projectTask21.setName("2.1");
        projectTask21.setParentId(2);
        ProjectTask projectTask12 = new ProjectTaskImpl();
        projectTask12.setId(4);
        projectTask12.setLevelOrder(2);
        projectTask12.setName("1.2");
        projectTask12.setParentId(7); // when parentId = 7, then the list will be looping
        ProjectTask projectTask121 = new ProjectTaskImpl();
        projectTask121.setId(6);
        projectTask121.setLevelOrder(1);
        projectTask121.setName("1.2.1");
        projectTask121.setParentId(4);
        ProjectTask projectTask1211 = new ProjectTaskImpl();
        projectTask1211.setId(7);
        projectTask1211.setLevelOrder(1);
        projectTask1211.setName("1.2.1.1");
        projectTask1211.setParentId(6);

        projectTasksWithoutLooping.add(projectTask1);
        projectTasksWithoutLooping.add(projectTask2);
        projectTasksWithoutLooping.add(projectTask11);
        projectTasksWithoutLooping.add(projectTask21);

        projectTasksWithLooping.addAll(projectTasksWithoutLooping);
        projectTasksWithLooping.add(projectTask12);
        projectTasksWithLooping.add(projectTask121);
        projectTasksWithLooping.add(projectTask1211);

        cycle.add(projectTask12);
        cycle.add(projectTask121);
        cycle.add(projectTask1211);

    }

    @Test
    void returnEmptySetWhenTheTreeItemListContainsNoCyclicity() {

        // project tasks list without looping
        Tree<ProjectTask> tree = new SimpleTree<>(projectTasksWithoutLooping, ProjectTask::getId, ProjectTask::getParentId);
        Unloopable unloopable = new UnloopableImpl();
        Set<ProjectTask> cycledItems = unloopable.detectCycle(tree.getTreeItems());
        assertTrue(cycledItems.isEmpty());

    }

    @Test
    void returnSetOfElementOfCycleWhenTheTreeListContainCyclicity() {

        Unloopable unloopable = new UnloopableImpl();
        // project tasks list with looping
        Tree<ProjectTask> tree = new SimpleTree<>(projectTasksWithLooping, ProjectTask::getId, ProjectTask::getParentId);

        Set<ProjectTask> cycledItem = unloopable.detectCycle(tree.getTreeItems());
        assertTrue(cycle.containsAll(cycledItem));

    }

}