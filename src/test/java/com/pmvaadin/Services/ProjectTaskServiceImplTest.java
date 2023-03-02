package com.pmvaadin.Services;

import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.services.ProjectTaskService;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
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



    }

    @Test
    void getProjectTasks() {

        Mockito.when(projectTaskRepository.findAllByOrderByLevelOrderAsc()).thenReturn(projectTasks);

//        TreeItem<ProjectTask> projectTaskList = null;
//        try {
//            projectTaskList = projectTaskService.getTreeProjectTasks();
//        } catch (Exception e) {
//
//        }
//        assertEquals(projectTaskList, projectTasks);
//
//        // get cycle in list
//        projectTask12.setParentId(7);
//
//        try {
//            projectTaskList = projectTaskService.getTreeProjectTasks();
//        } catch (Exception e) {
//
//        }
//
//        assertEquals(projectTaskList, projectTasks);

    }
}