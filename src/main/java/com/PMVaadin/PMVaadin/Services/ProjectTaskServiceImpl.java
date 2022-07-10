package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import com.PMVaadin.PMVaadin.ProjectStructure.*;
import com.PMVaadin.PMVaadin.Repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository){
        this.projectTaskRepository = projectTaskRepository;
    }

    @Override
    public List<ProjectTask> getProjectTasks() {
        List<ProjectTask> projectTasks = projectTaskRepository.findAll();

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
        ProjectTask projectTask12 = new ProjectTaskImpl();
        projectTask12.setId(4);
        projectTask12.setLevelOrder(2);
        projectTask12.setName("1.2");
        projectTask12.setParentId(7); // when parentId = 7, then the list will become the list with circle
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

        TreeProjectTasks<ProjectTask> treeProjectTasks = new TreeProjectTasksImpl<>();
        treeProjectTasks.populateTreeByList(projectTasks);
        ValidationsMessage validations = treeProjectTasks.validateTree();

        if (!validations.validationPassed()) projectTasks.clear();

        treeProjectTasks.fillWbs();

        return projectTasks;

    }



}
