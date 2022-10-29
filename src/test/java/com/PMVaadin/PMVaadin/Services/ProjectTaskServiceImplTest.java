package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTask;
import com.PMVaadin.PMVaadin.ProjectTasks.Entity.ProjectTaskImpl;
import com.PMVaadin.PMVaadin.ProjectTasks.Services.ProjectTaskService;
import com.PMVaadin.PMVaadin.CommonObjects.Tree.TreeItem;
import com.PMVaadin.PMVaadin.ProjectTasks.Repositories.ProjectTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest
class ProjectTaskServiceImplTest {

    private List<ProjectTask> projectTasks;
    private ProjectTask projectTask12;
    private ProjectTaskRepository projectTaskRepository;
    private ProjectTaskService projectTaskService;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository){
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setProjectTaskService(ProjectTaskService projectTaskService){
        this.projectTaskService = projectTaskService;
    }

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
        projectTask12 = new ProjectTaskImpl();
        projectTask12.setId(4);
        projectTask12.setLevelOrder(2);
        projectTask12.setName("1.2");
        projectTask12.setParentId(1); // when parentId = 7, then the list will become the list with circle
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
        ProjectTask projectTask21 = new ProjectTaskImpl();
        projectTask21.setId(5);
        projectTask21.setLevelOrder(1);
        projectTask21.setName("2.1");
        projectTask21.setParentId(2);
        projectTasks.add(projectTask1);
        projectTasks.add(projectTask2);
        projectTasks.add(projectTask11);
        projectTasks.add(projectTask12);
        projectTasks.add(projectTask121);
        projectTasks.add(projectTask1211);
        projectTasks.add(projectTask21);

    }

    @Test
    void getProjectTasks() {

        Mockito.when(projectTaskRepository.findAllByOrderByLevelOrderAsc()).thenReturn(projectTasks);

        TreeItem<ProjectTask> projectTaskList = null;
        try {
            projectTaskList = projectTaskService.getTreeProjectTasks();
        } catch (Exception e) {

        }
        assertEquals(projectTaskList, projectTasks);

        // get circle in list
        projectTask12.setParentId(7);

        try {
            projectTaskList = projectTaskService.getTreeProjectTasks();
        } catch (Exception e) {

        }

        assertEquals(projectTaskList, projectTasks);

    }
}