package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
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
    public List<ProjectTask> getProjectTasks() throws Exception {

        List<ProjectTask> projectTasks = projectTaskRepository.findAll();
        TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(projectTasks);
        treeProjectTasks.validateTree();
        treeProjectTasks.fillWbs();

        return projectTasks;

    }

    @Override
    public ProjectTask saveTask(ProjectTask projectTask) {

        if (projectTask.isNew()) {
            Integer parentId = projectTask.getParentId();
            Integer levelOrder = 0;
            if (parentId == null) {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevelWhereParentNull();
            } else {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(projectTask.getParentId());
            }
            if (levelOrder == null) levelOrder = 0;
            projectTask.setLevelOrder(++levelOrder);
        }

        return projectTaskRepository.save(projectTask);
    }

}
