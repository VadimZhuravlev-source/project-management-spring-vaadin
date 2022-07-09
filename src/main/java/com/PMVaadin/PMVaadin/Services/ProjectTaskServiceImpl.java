package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import com.PMVaadin.PMVaadin.ProjectStructure.SimpleTree;
import com.PMVaadin.PMVaadin.ProjectStructure.TreeItem;
import com.PMVaadin.PMVaadin.ProjectStructure.TreeProjectTasks;
import com.PMVaadin.PMVaadin.ProjectStructure.TreeProjectTasksImpl;
import com.PMVaadin.PMVaadin.Repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        //List<ProjectTask> projectTaskList = projectTasks.stream().collect(Collectors.toList());

        //List<ProjectTask> projectTasks = new ArrayList<>();
        ProjectTask projectTask1 = new ProjectTaskImpl();
        projectTask1.setId(1);
        projectTask1.setLevelOrder(1);
        projectTask1.setName("1");
        ProjectTask projectTask2 = new ProjectTaskImpl();
        projectTask2.setId(2);
        projectTask2.setLevelOrder(2);
        projectTask2.setName("2");
        ProjectTask projectTask12 = new ProjectTaskImpl();
        projectTask12.setId(3);
        projectTask12.setLevelOrder(1);
        projectTask12.setName("1.2");
        projectTask12.setParentId(1);
        ProjectTask projectTask13 = new ProjectTaskImpl();
        projectTask13.setId(4);
        projectTask13.setLevelOrder(2);
        projectTask13.setName("1.3");
        projectTask13.setParentId(1);
        ProjectTask projectTask21 = new ProjectTaskImpl();
        projectTask21.setId(5);
        projectTask21.setLevelOrder(1);
        projectTask21.setName("2.1");
        projectTask21.setParentId(2);
        projectTasks.add(projectTask1);
        projectTasks.add(projectTask2);
        projectTasks.add(projectTask12);
        projectTasks.add(projectTask13);
        projectTasks.add(projectTask21);

        SimpleTree<ProjectTask> treeItem = new TreeItem<>();
        SimpleTree<ProjectTask> rootTreeItem = treeItem.getTreeByList(projectTasks, ProjectTask::getId, ProjectTask::getParentId);
        TreeProjectTasks<ProjectTask> treeProjectTasks = new TreeProjectTasksImpl<>();
        //SimpleTree<ProjectTask> rootTreeItem = treeProjectTasks.getTreeByList(projectTasks, ProjectTask::getId, ProjectTask::getParentId);
        treeProjectTasks.fillWbs(rootTreeItem);

        return projectTasks;

    }
}
