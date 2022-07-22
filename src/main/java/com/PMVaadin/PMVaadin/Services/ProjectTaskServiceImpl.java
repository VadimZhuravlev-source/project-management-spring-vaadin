package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import com.PMVaadin.PMVaadin.ProjectStructure.*;
import com.PMVaadin.PMVaadin.Repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private ProjectTaskRepository projectTaskRepository;
    private EntityManagerService entityManagerService;
    private TreeProjectTasks treeProjectTasks;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository){
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setEntityManagerService(EntityManagerService entityManagerService){
        this.entityManagerService = entityManagerService;
    }

    @Autowired
    public void setTreeProjectTasks(TreeProjectTasks treeProjectTasks){
        this.treeProjectTasks = treeProjectTasks;
    }

    @Override
    public TreeItem<ProjectTask> getTreeProjectTasks() throws Exception {

        List<ProjectTask> projectTasks = projectTaskRepository.findAllByOrderByLevelOrderAsc();
        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
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
        } else {
            Optional<ProjectTask> foundValue = projectTaskRepository.findById(projectTask.getId());
            ProjectTask foundTask = foundValue.orElse(null);
            if (foundTask == null) throw new Exception("Incorrect data. Update project and try again.");
        }

        if (projectTask.getId() != null && projectTask.getId().equals(projectTask.getParentId()))
            throw new Exception("Incorrect data. Update project and try again.");

        return projectTaskRepository.save(projectTask);

    }

    @Override
    public void deleteTasks(List<? extends ProjectTask> projectTasks) {

        List<Integer> parentIds = projectTasks.stream().map(ProjectTask::getParentId).toList();
        List<ProjectTask> projectTaskForDeletion = getProjectTasksToDeletion(projectTasks);
        Map<Integer, Boolean> parentIdsForDeletion =
                projectTaskForDeletion.stream().collect(Collectors.toMap(ProjectTask::getId, projectTask -> true));
        parentIds = parentIds.stream().filter(integer -> Objects.isNull(parentIdsForDeletion.get(integer))).collect(Collectors.toList());
        List<Integer> projectTaskIds = projectTaskForDeletion.stream().map(ProjectTask::getId).toList();

        transactionalDeletionAndRecalculation(projectTaskIds, parentIds);

    }

    @Override
    public void recalculateProject() {

        List<ProjectTask> projectTasks = projectTaskRepository.findAllByOrderByLevelOrderAsc();
        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(projectTasks);
        List<ProjectTask> savedElements = treeProjectTasks.recalculateThePropertiesOfTheWholeProject();
        projectTaskRepository.saveAll(savedElements);

    }

    @Override
    public void setNewParentOfTheTasks(Set<? extends ProjectTask> projectTasks, ProjectTask parent) {

        List<ProjectTask> projectTaskList = new ArrayList<>(projectTasks);
        projectTaskList.add(parent);

        List<Integer> ids = projectTaskList.stream().map(ProjectTask::getId).collect(Collectors.toList());

        Map<ProjectTask, ProjectTask> foundProjectTasks =
                projectTaskRepository.findAllById(ids).stream().collect(Collectors.toMap(p -> p, p -> p));

        if (foundProjectTasks.size() != projectTaskList.size())
            throw new IllegalStateException("Incorrect data. Should update project and try again.");

        ProjectTask parentInBase = foundProjectTasks.get(parent);

        if (parentInBase == null)
            throw new IllegalStateException("Incorrect data. Should update project and try again.");

        if (!parent.getVersion().equals(parentInBase.getVersion()))
            throw new IllegalStateException("The task " + parent + " has been changed by another user. Should update project and try again.");

        Map<ProjectTask, ProjectTask> parentsOfParentMap = entityManagerService.getParentsOfParent(parentInBase).stream().collect(
                Collectors.toMap(p -> p, p -> p));

        List<Integer> parentIds = new ArrayList<>();
        Integer levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parent.getId());
        if (levelOrder == null) levelOrder = 0;
        projectTaskList = new ArrayList<>(projectTasks);
        for (ProjectTask projectTask: projectTaskList) {
            if (parentsOfParentMap.get(projectTask) != null)
                // Detected circle in tree
                throw new IllegalStateException("Project structure has been changed. Should update project and try again.");
            ProjectTask projectTasksInBase = foundProjectTasks.get(projectTask);
            if (!projectTask.getVersion().equals(projectTasksInBase.getVersion()))
                throw new IllegalStateException("The task " + projectTask + " has been changed by another user. Should update project and try again.");
            parentIds.add(projectTask.getParentId());
            projectTask.setParentId(parent.getId());
            projectTask.setLevelOrder(++levelOrder);
        }

        transactionalSaveAndRecalculation(projectTaskList, parentIds);

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

        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        return treeProjectTasks.recalculateLevelOrderForProjectTasks(foundProjectTasks);

    }

    @Transactional
    private void transactionalSaveAndRecalculation(List<ProjectTask> projectTaskList, List<Integer> parentIds) {

        projectTaskRepository.saveAll(projectTaskList);
        List<ProjectTask> savedElements = recalculateForChildrenOfProjectTaskIds(parentIds);
        projectTaskRepository.saveAll(savedElements);

    }

    @Transactional
    private void transactionalDeletionAndRecalculation(List<Integer> projectTaskIds, List<Integer> parentIds) {

        projectTaskRepository.deleteAllById(projectTaskIds);
        List<ProjectTask> savedElements = recalculateForChildrenOfProjectTaskIds(parentIds);
        projectTaskRepository.saveAll(savedElements);

    }

    private List<ProjectTask> getProjectTasksToDeletion(List<? extends ProjectTask> projectTasks) {

        projectTasks = projectTasks.stream().distinct().collect(Collectors.toList());

        if (projectTasks.size() == 0) {
            return new ArrayList<>();
        }

        List<ProjectTask> allHierarchyElements = entityManagerService.getElementsChildrenInDepth(projectTasks);

        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
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

}
