package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.ProjectStructure.*;
import com.PMVaadin.PMVaadin.Repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private ProjectTaskRepository projectTaskRepository;
    private EntityManagerService entityManagerService;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository){
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setEntityManagerService(EntityManagerService entityManagerService){
        this.entityManagerService = entityManagerService;
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
    public ProjectTask saveTask(ProjectTask projectTask) throws Exception {

        if (projectTask.isNew()) {
            Integer parentId = projectTask.getParentId();
            Integer levelOrder = 0;
            if (parentId == null) {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevelWhereParentNull();
            } else {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parentId);
            }
            if (levelOrder == null) levelOrder = 0;
            projectTask.setLevelOrder(++levelOrder);
        }

        if (projectTask.getId() == projectTask.getParentId())
            throw new Exception("Incorrect data. Update project and try again.");

        return projectTaskRepository.save(projectTask);

    }

    @Override
    public void deleteTasks(List<ProjectTask> projectTasks) throws Exception {

        List<Integer> projectTaskIds = prepareHierarchyElementsToDelete(projectTasks);
        projectTaskRepository.deleteAllById(projectTaskIds);

    }

    private List<Integer> prepareHierarchyElementsToDelete(List<ProjectTask> projectTasks) {

        projectTasks = projectTasks.stream().distinct().collect(Collectors.toList());

        if (projectTasks.size() == 0) {
            return new ArrayList<>();
        }

        List<ProjectTask> allHierarchyElements = entityManagerService.getElementsChildrenInDepth(projectTasks);

        TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(allHierarchyElements);
        TreeItem<ProjectTask> rootItem = treeProjectTasks.getRootItem();

        List<Integer> projectTaskIds = new ArrayList<>(allHierarchyElements.size());
        fillDeletedProjectTasksRecursively(rootItem, projectTaskIds);

        return projectTaskIds;

    }

    private void fillDeletedProjectTasksRecursively(TreeItem<ProjectTask> treeItem, List<Integer> projectTaskIds) {

        for (TreeItem<ProjectTask> currentTreeItem: treeItem.getChildren()) {
            fillDeletedProjectTasksRecursively(currentTreeItem, projectTaskIds);
            projectTaskIds.add(currentTreeItem.getValue().getId());
        }

    }

}
