package com.pmvaadin.projecttasks.services;

import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
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
    public List<ProjectTask> getTreeProjectTasks() {

        List<ProjectTask> projectTasks = projectTaskRepository.findAllByOrderByLevelOrderAsc();
        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(projectTasks);
        treeProjectTasks.validateTree();
        treeProjectTasks.fillWbs();

        projectTasks.clear();
        populateListByRootItemRecursively(projectTasks, treeProjectTasks.getRootItem());

        return projectTasks;

    }

    @Override
    public ProjectTask save(ProjectTask projectTask, boolean validate, boolean recalculateTerms) {

        if (validate && !validate(projectTask)) return projectTask;
        ProjectTask savedProjectTask = projectTaskRepository.save(projectTask);
        if (!recalculateTerms) {
            return savedProjectTask;
        }
        recalculateTerms();
        return savedProjectTask;

    }

    @Override
    public boolean validate(ProjectTask projectTask) {

        // Validate parent existence
        Integer parentId = projectTask.getParentId();
        if (parentId != null) {
            Optional<ProjectTask> foundParentValue = projectTaskRepository.findById(projectTask.getParentId());
            ProjectTask foundParent = foundParentValue.orElse(null);
            if (foundParent == null)
                throw new StandardError("Unable to persist the object. A Parent of the task does not exist. Update the project and try again.");
        }

        if (projectTask.isNew()) {
            parentId = projectTask.getParentId();
            Integer levelOrder;
            if (parentId == null) {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevelWhereParentNull();
            } else {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parentId);
            }
            if (levelOrder == null) levelOrder = 0;
            projectTask.setLevelOrder(++levelOrder);
        } else {
            if (projectTask.getId().equals(projectTask.getParentId()))
                throw new StandardError("Unable to persist the object. Update the project and try again.");
            Optional<ProjectTask> foundValue = projectTaskRepository.findById(projectTask.getId());
            ProjectTask foundTask = foundValue.orElse(null);
            if (foundTask == null) throw new StandardError("Unable to persist the object. Update the project and try again.");
            if (!foundTask.getVersion().equals(projectTask.getVersion()))
                throw new StandardError("Unable to persist the object. The object was changed by an another user. Update the project and try again.");
        }

        return true;

    }

    @Override
    public void delete(List<? extends ProjectTask> projectTasks) {

        List<?> parentIds = projectTasks.stream().map(ProjectTask::getParentId).toList();
        List<ProjectTask> projectTaskForDeletion = getProjectTasksToDeletion(projectTasks);
        Map<?, Boolean> parentIdsForDeletion =
                projectTaskForDeletion.stream().collect(Collectors.toMap(ProjectTask::getId, projectTask -> true));
        parentIds = parentIds.stream().filter(parentId -> Objects.isNull(parentIdsForDeletion.get(parentId))).collect(Collectors.toList());
        List<?> projectTaskIds = projectTaskForDeletion.stream().map(ProjectTask::getId).toList();

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
    public void changeParent(Set<? extends ProjectTask> projectTasks, ProjectTask parent) {

        List<ProjectTask> projectTaskList = new ArrayList<>(projectTasks);
        projectTaskList.add(parent);

        List<?> ids = projectTaskList.stream().map(ProjectTask::getId).collect(Collectors.toList());

        Map<ProjectTask, ProjectTask> foundProjectTasks =
                projectTaskRepository.findAllById(ids).stream().collect(Collectors.toMap(p -> p, p -> p));

        if (foundProjectTasks.size() != projectTaskList.size())
            throw new StandardError("Incorrect data. Should update the project and try again.");

        ProjectTask parentInBase = foundProjectTasks.get(parent);

        if (parentInBase == null)
            throw new StandardError("Incorrect data. Should update the project and try again.");

        if (!parent.getVersion().equals(parentInBase.getVersion()))
            throw new StandardError("The task " + parent + " has been changed by an another user. Should update the project and try again.");

        Map<ProjectTask, ProjectTask> parentsOfParentMap = entityManagerService.getParentsOfParent(parentInBase).stream().collect(
                Collectors.toMap(p -> p, p -> p));

        var parentIds = new ArrayList<>();
        Integer levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parent.getId());
        if (levelOrder == null) levelOrder = 0;
        projectTaskList = new ArrayList<>(projectTasks);
        for (ProjectTask projectTask: projectTaskList) {
            if (parentsOfParentMap.get(projectTask) != null)
                // Detected circle in tree
                throw new StandardError("Project structure has been changed. Should update the project and try again.");
            ProjectTask projectTasksInBase = foundProjectTasks.get(projectTask);
            if (!projectTask.getVersion().equals(projectTasksInBase.getVersion()))
                throw new StandardError("The task " + projectTask + " has been changed by an another user. Should update the project and try again.");
            parentIds.add(projectTask.getParentId());
            projectTask.setParentId(parent.getId());
            projectTask.setLevelOrder(++levelOrder);
        }

        transactionalSaveAndRecalculation(projectTaskList, parentIds);

    }

    @Override
    public List<? extends ProjectTask> swap(Map<? extends ProjectTask, ? extends ProjectTask> swappedTasks) {

        ProjectTask projectTask1 = null, projectTask2 = null;
        for (Map.Entry<? extends ProjectTask, ? extends ProjectTask> kv: swappedTasks.entrySet()) {
            projectTask1 = kv.getKey();
            projectTask2 = kv.getValue();
            break;
        }

        List<Integer> ids = new ArrayList<>(2);
        ids.add(projectTask1.getId());
        ids.add(projectTask2.getId());

        List<ProjectTask> projectTasksInBase = projectTaskRepository.findAllById(ids);

        if (projectTasksInBase.size() != 2)
            throw new StandardError("Roaming tasks do not exist. Should update project and try again.");

        ProjectTask projectTask1InBase = null;
        ProjectTask projectTask2InBase = null;
        for (ProjectTask projectTask: projectTasksInBase) {
            if (projectTask.equals(projectTask1)) projectTask1InBase = projectTask;
            if (projectTask.equals(projectTask2)) projectTask2InBase = projectTask;
        }

        if (!projectTask1InBase.getVersion().equals(projectTask1.getVersion()) ||
        !projectTask2InBase.getVersion().equals(projectTask2.getVersion())) {
            throw new StandardError("Roaming tasks are changed by an another user. Should update the project and try again.");
        }

        Integer levelOrder = projectTask1InBase.getLevelOrder();
        projectTask1InBase.setLevelOrder(projectTask2InBase.getLevelOrder());
        projectTask2InBase.setLevelOrder(levelOrder);

        List<ProjectTask> savedTasks = projectTaskRepository.saveAll(projectTasksInBase);

        for (ProjectTask projectTask: savedTasks) {
            if (projectTask.equals(projectTask1)) projectTask.setWbs(projectTask2.getWbs());
            if (projectTask.equals(projectTask2)) projectTask.setWbs(projectTask1.getWbs());
        }

        return savedTasks;

//        return projectTaskRepository.replaceTasks(projectTask1.getId(), projectTask1.getVersion(),
//                projectTask2.getId(), projectTask2.getVersion());

    }

    @Override
    public ProjectTask sync(ProjectTask projectTask) {
        return projectTaskRepository.findById(projectTask.getId()).orElse(null);
    }

    @Override
    public int getChildrenCount(ProjectTask projectTask) {
        if (Objects.isNull(projectTask) || Objects.isNull(projectTask.getId())) {
            return projectTaskRepository.getChildrenCount();
        }
        return projectTaskRepository.getChildrenCount(projectTask.getId());
    }

    @Override
    public boolean hasChildren(ProjectTask projectTask) {
        return getChildrenCount(projectTask) != 0;
    }

    @Override
    public List<ProjectTask> fetchChildren(ProjectTask projectTask) {

        List<ProjectTask> projectTasks;
        if (Objects.isNull(projectTask) || Objects.isNull(projectTask.getId())) {
            projectTasks = projectTaskRepository.findByParentIdIsNullOrderByLevelOrderAsc();
        } else {
            projectTasks = projectTaskRepository.findByParentIdOrderByLevelOrderAsc(projectTask.getId());
        }
        fillWbs(projectTasks, projectTask);
        return projectTasks;
    }

    @Override
    public Map<Integer, ProjectTask> getProjectTasksWithWbs(List<Integer> ids) {

        if (ids.size() == 0) return new HashMap<>();

        List<ProjectTask> projectTasks = entityManagerService.getParentsOfParent(ids);
        treeProjectTasks.populateTreeByList(projectTasks);
        treeProjectTasks.fillWbs();
        return projectTasks.stream().
                filter(projectTask -> ids.contains(projectTask.getId())).
                collect(Collectors.toMap(ProjectTask::getId, p -> p));

    }

    @Override
    @Transactional
    public void increaseTaskLevel(Set<ProjectTask> projectTasks) {

        if (projectTasks.size() == 0) return;

        // do only for one task
        var projectTask = projectTasks.stream().findFirst().orElse(null);
        if (projectTask == null) return;

        var syncProjectTask = projectTaskRepository.findById(projectTask.getId()).orElse(null);
        if (syncProjectTask == null) return; // TODO throw exception
        if (!syncProjectTask.getVersion().equals(projectTask.getVersion())) return; // TODO throw exception

        var parentId = syncProjectTask.getParentId();
        if (parentId == null) return;
        var parent = projectTaskRepository.findById(parentId).orElse(null);

        // TODO validate links

        if (parent == null) return;
        var parentOfParentId = parent.getParentId();

        var maxLevelOrder = 0;
        if (parentOfParentId == null) maxLevelOrder = projectTaskRepository.findMaxOrderIdOnParentLevelWhereParentNull();
        else maxLevelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parentOfParentId);

        syncProjectTask.setParentId(parentOfParentId);
        syncProjectTask.setLevelOrder(++maxLevelOrder);

        projectTaskRepository.save(syncProjectTask);

        // TODO change parent for all selected tasks
//        var ids = projectTasks.stream().map(ProjectTask::getId).toList();
//        var syncedProjectTasks = projectTaskRepository.findAllById(ids).stream()
//                .filter(projectTask -> projectTask.getParentId() != null)
//                .collect(Collectors.toMap(p -> p, p -> p));
//        validateVersion(projectTasks, syncedProjectTasks);
//
//        validateLinks();
//        recalculateTerms();
//
//        var parentIds = syncedProjectTasks.keySet().stream().map(ProjectTask::getParentId).toList();
//
//        var parents = projectTaskRepository.findAllById(parentIds);
//
//        var parentsOfParentsHighLevel = parents.stream()
//                .filter(projectTask -> projectTask.getParentId() == null).toList();
//        var parentsOfParents = parents.stream().filter(projectTask -> projectTask.getParentId() != null).toList();
//
//
//        var groupedTasks = syncedProjectTasks.keySet().stream().c

    }

    @Override
    public void decreaseTaskLevel(Set<ProjectTask> projectTasks) {

        validateLinks();
        recalculateTerms();

    }

    private void validateVersion(Collection<ProjectTask> projectTasks, Map<ProjectTask, ProjectTask> projectTasksInBase) {

        projectTasks.forEach(projectTask -> {
            ProjectTask projectTaskInBase = projectTasksInBase.getOrDefault(projectTask, null);
            if (projectTaskInBase == null) return;
            if (!projectTask.getVersion().equals(projectTaskInBase.getVersion()))
                throw new StandardError("The task " + projectTask + " has been changed by an another user. Should update the project and try again.");
        });

    }

    private void fillWbs(List<ProjectTask> children, ProjectTask projectTask) {

        String wbs = "";
        if (!Objects.isNull(projectTask)) wbs = projectTask.getWbs() + ".";
        final String parentWbs = wbs;
        children.forEach(child -> child.setWbs(parentWbs + child.getLevelOrder()));

    }

    private void recalculateTerms() {
        // TODO recalculate terms
    }

    private void validateLinks() {
        // TODO validate terms
    }

    private void populateListByRootItemRecursively(List<ProjectTask> projectTasks, TreeItem<ProjectTask> treeItem) {

        for (TreeItem<ProjectTask> child: treeItem.getChildren()) {
            projectTasks.add(child.getValue());
            populateListByRootItemRecursively(projectTasks, child);
        }

    }

    private List<ProjectTask> recalculateForChildrenOfProjectTaskIds(List<?> parentIds) {

        List<ProjectTask> foundProjectTasks;

        if (parentIds.stream().anyMatch(Objects::isNull)) {
            List<?> findingParentIds = parentIds.stream().filter(Objects::nonNull).collect(Collectors.toList());
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
    private void transactionalSaveAndRecalculation(List<ProjectTask> projectTaskList, List<?> parentIds) {

        projectTaskRepository.saveAll(projectTaskList);
        List<ProjectTask> savedElements = recalculateForChildrenOfProjectTaskIds(parentIds);
        projectTaskRepository.saveAll(savedElements);

    }

    @Transactional
    private void transactionalDeletionAndRecalculation(List<?> projectTaskIds, List<?> parentIds) {

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