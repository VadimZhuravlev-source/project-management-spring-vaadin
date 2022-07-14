package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import com.PMVaadin.PMVaadin.ProjectStructure.*;
import com.PMVaadin.PMVaadin.Repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public TreeItem<ProjectTask> getTreeProjectTasks() throws Exception {

        List<ProjectTask> projectTasks = projectTaskRepository.findAllByOrderByLevelOrderAsc();
        TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(projectTasks);
        treeProjectTasks.validateTree();
        treeProjectTasks.fillWbs();

        return treeProjectTasks.getRootItem();

    }

    @Override
    public ProjectTask saveTask(ProjectTask projectTask) throws Exception {

        if (projectTask.isNew()) {
            Integer parentId = projectTask.getParentId();
            Integer levelOrder;
            if (parentId == null) {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevelWhereParentNull();
            } else {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parentId);
            }
            if (levelOrder == null) levelOrder = 0;
            projectTask.setLevelOrder(++levelOrder);
        }

        if (projectTask.getId() != null && projectTask.getId().equals(projectTask.getParentId()))
            throw new Exception("Incorrect data. Update project and try again.");

        return projectTaskRepository.save(projectTask);

    }

    @Override
    public void deleteTasks(List<ProjectTask> projectTasks) throws Exception {

        List<Integer> parentIds = projectTasks.stream().map(ProjectTask::getParentId).toList();
        List<ProjectTask> projectTaskForDeletion = getProjectTasksToDeletion(projectTasks);
        Map<Integer, Boolean> parentIdsForDeletion =
                projectTaskForDeletion.stream().collect(Collectors.toMap(ProjectTask::getId, projectTask -> true));
        parentIds = parentIds.stream().filter(integer -> Objects.isNull(parentIdsForDeletion.get(integer))).collect(Collectors.toList());
        List<Integer> projectTaskIds = projectTaskForDeletion.stream().map(ProjectTask::getId).toList();

        transactionalActsWithTasks(projectTaskIds, parentIds);

    }

    @Override
    public void recalculateProject() {

        List<ProjectTask> projectTasks = projectTaskRepository.findAllByOrderByLevelOrderAsc();
        List<ProjectTask> savedElements = recalculateProjectProperties(projectTasks);
        projectTaskRepository.saveAll(savedElements);

    }

    private List<ProjectTask> recalculateForChildrenOfProjectTaskIds(List<Integer> parentIds) {

        List<ProjectTask> foundProjectTasks;

        if (parentIds.stream().anyMatch(Objects::isNull)) {
            List<Integer> findingParentIds = parentIds.stream().filter(Objects::nonNull).collect(Collectors.toList());
            List<ProjectTaskImpl> foundProjectTasks1 =
                    projectTaskRepository.findByParentIdInWithNullOrderByLevelOrderAsc(findingParentIds);
            foundProjectTasks = foundProjectTasks1.stream()
                    .map(projectTaskImpl -> (ProjectTask) projectTaskImpl)
                    .collect(Collectors.toList());
        } else {
            foundProjectTasks = projectTaskRepository.findByParentIdInOrderByLevelOrderAsc(parentIds);
        }

        return recalculateProjectProperties(foundProjectTasks);

    }

    @Transactional
    private void transactionalActsWithTasks(List<Integer> projectTaskIds, List<Integer> parentIds) {

        projectTaskRepository.deleteAllById(projectTaskIds);
        List<ProjectTask> savedElements = recalculateForChildrenOfProjectTaskIds(parentIds);
        projectTaskRepository.saveAll(savedElements);

    }

    private List<ProjectTask> getProjectTasksToDeletion(List<ProjectTask> projectTasks) {

        projectTasks = projectTasks.stream().distinct().collect(Collectors.toList());

        if (projectTasks.size() == 0) {
            return new ArrayList<>();
        }

        List<ProjectTask> allHierarchyElements = entityManagerService.getElementsChildrenInDepth(projectTasks);

        TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(allHierarchyElements);
        TreeItem<ProjectTask> rootItem = treeProjectTasks.getRootItem();

        List<ProjectTask> projectTaskIds = new ArrayList<>(allHierarchyElements.size());
        fillDeletedProjectTasksRecursively(rootItem, projectTaskIds);

        return projectTaskIds;

    }

    private void fillDeletedProjectTasksRecursively(TreeItem<ProjectTask> treeItem, List<ProjectTask> projectTaskIds) {

        for (TreeItem<ProjectTask> currentTreeItem: treeItem.getChildren()) {
            fillDeletedProjectTasksRecursively(currentTreeItem, projectTaskIds);
            projectTaskIds.add(currentTreeItem.getValue());
        }

    }

    private List<ProjectTask> recalculateProjectProperties(List<ProjectTask> projectTasks) {

        TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(projectTasks);
        return treeProjectTasks.recalculateProjectProperties();

    }

}
