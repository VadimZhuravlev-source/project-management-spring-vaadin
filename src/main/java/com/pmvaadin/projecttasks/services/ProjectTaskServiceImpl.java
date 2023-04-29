package com.pmvaadin.projecttasks.services;

import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projectstructure.TestCase;
import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.projecttasks.dependencies.DependenciesService;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskOrderedHierarchy;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private ProjectTaskRepository projectTaskRepository;
    private HierarchyService hierarchyService;
    private TreeProjectTasks treeProjectTasks;

    private DependenciesService dependenciesService;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository){
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setHierarchyService(HierarchyService hierarchyService){
        this.hierarchyService = hierarchyService;
    }

    @Autowired
    public void setTreeProjectTasks(TreeProjectTasks treeProjectTasks){
        this.treeProjectTasks = treeProjectTasks;
    }

    @Autowired
    public void setDependenciesService(DependenciesService dependenciesService){
        this.dependenciesService = dependenciesService;
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
        fillNecessaryFieldsIfItIsNew(projectTask);
        ProjectTask savedProjectTask = projectTaskRepository.save(projectTask);
        if (!recalculateTerms) {
            return savedProjectTask;
        }
        recalculateTerms(savedProjectTask);
        return savedProjectTask;

    }

    @Override
    public boolean validate(ProjectTask projectTask) {

        if (!projectTask.isNew()) {
            if (projectTask.getId().equals(projectTask.getParentId()))
                throw new StandardError("Unable to persist the object. Update the project and try again.");
            Optional<ProjectTask> foundValue = projectTaskRepository.findById(projectTask.getId());
            ProjectTask foundTask = foundValue.orElse(null);
            if (foundTask == null) throw new StandardError("The project task was deleted.");
            if (!foundTask.getVersion().equals(projectTask.getVersion()))
                throw new StandardError("Unable to persist the object. The object was changed by an another user. Update the project and try again.");
        }

        return true;

    }

    @Override
    @Transactional
    public void delete(List<? extends ProjectTask> projectTasks) {

        List<?> parentIds = projectTasks.stream().map(ProjectTask::getParentId).toList();
        List<ProjectTask> projectTaskForDeletion = getProjectTasksToDeletion(projectTasks);
        Map<?, Boolean> parentIdsForDeletion =
                projectTaskForDeletion.stream().collect(Collectors.toMap(ProjectTask::getId, projectTask -> true));
        parentIds = parentIds.stream().filter(parentId -> Objects.isNull(parentIdsForDeletion.get(parentId))).collect(Collectors.toList());
        List<?> projectTaskIds = projectTaskForDeletion.stream()
                .sorted(Comparator.comparing(ProjectTaskOrderedHierarchy::getWbs).reversed())
                .map(ProjectTask::getId).toList();

        projectTaskRepository.deleteAllById(projectTaskIds);
        List<ProjectTask> savedElements = recalculateLevelOrderForChildrenOfProjectTaskIds(parentIds);
        projectTaskRepository.saveAll(savedElements);

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
    public Set<ProjectTask> changeLocation(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        Set<ProjectTask> changedTasks = changeLocationInner(projectTasks, target, dropLocation);

        return getProjectTasksForSelection(projectTasks, changedTasks);

    }

    @Override
    public Set<ProjectTask> changeSortOrder(Set<ProjectTask> projectTasks, Direction direction) {

        Set<ProjectTask> changedTasks = changedSortOrderTransactional(projectTasks, direction);

        return getProjectTasksForSelection(projectTasks, changedTasks);

//        ProjectTask projectTask1 = null, projectTask2 = null;
//        for (Map.Entry<ProjectTask, ProjectTask> kv: tasks.entrySet()) {
//            projectTask1 = kv.getKey();
//            projectTask2 = kv.getValue();
//            break;
//        }
//
//        List<Integer> ids = new ArrayList<>(2);
//        ids.add(projectTask1.getId());
//        ids.add(projectTask2.getId());
//
//        List<ProjectTask> projectTasksInBase = projectTaskRepository.findAllById(ids);
//
//        if (projectTasksInBase.size() != 2)
//            throw new StandardError("Roaming tasks do not exist. Should update project and try again.");
//
//        ProjectTask projectTask1InBase = null;
//        ProjectTask projectTask2InBase = null;
//        for (ProjectTask projectTask: projectTasksInBase) {
//            if (projectTask.equals(projectTask1)) projectTask1InBase = projectTask;
//            if (projectTask.equals(projectTask2)) projectTask2InBase = projectTask;
//        }
//
//        if (!projectTask1InBase.getVersion().equals(projectTask1.getVersion()) ||
//        !projectTask2InBase.getVersion().equals(projectTask2.getVersion())) {
//            throw new StandardError("Roaming tasks are changed by an another user. Should update the project and try again.");
//        }
//
//        Integer levelOrder = projectTask1InBase.getLevelOrder();
//        projectTask1InBase.setLevelOrder(projectTask2InBase.getLevelOrder());
//        projectTask2InBase.setLevelOrder(levelOrder);
//
//        List<ProjectTask> savedTasks = projectTaskRepository.saveAll(projectTasksInBase);
//
//        for (ProjectTask projectTask: savedTasks) {
//            if (projectTask.equals(projectTask1)) projectTask.setWbs(projectTask2.getWbs());
//            if (projectTask.equals(projectTask2)) projectTask.setWbs(projectTask1.getWbs());
//        }
//
//        return savedTasks;
//
//        return projectTaskRepository.replaceTasks(projectTask1.getId(), projectTask1.getVersion(),
//                projectTask2.getId(), projectTask2.getVersion());

    }

    @Override
    public ProjectTask sync(ProjectTask projectTask) {
        return projectTaskRepository.findById(projectTask.getId()).orElse(null);
    }

    @Override
    public Map<?, ProjectTask> getProjectTasksByIdWithFilledWbs(List<?> ids) {

        if (ids.size() == 0) return new HashMap<>();

        ids = ids.stream().distinct().toList();

        var projectTasks = hierarchyService.getParentsOfParent(ids);
        treeProjectTasks.populateTreeByList(projectTasks);
        treeProjectTasks.fillWbs();
        Map<?, ?> filter = ids.stream().collect(Collectors.toMap(id -> id, id -> false));
        return projectTasks.stream().
                filter(projectTask -> filter.containsKey(projectTask.getId())).
                collect(Collectors.toMap(ProjectTask::getId, p -> p));

    }

    @Override
    @Transactional
    public void increaseTaskLevel(Set<ProjectTask> projectTasks) {

        if (projectTasks.size() == 0) return;


        //TODO need to do through changeLocationInner




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

        //TODO do through changeParent
        //validateLinks();

        //recalculateTerms(DependenciesSet dependenciesSet);

    }

    @Override
    public void createTestCase() {
        TestCase.createTestCase(this);
    }

    @Override
    public void fillParent(ProjectTask projectTask) {

        if (projectTask.getParentId() == null) {
            projectTask.setParent(null);
            return;
        }
        ProjectTask parent = projectTaskRepository.findById(projectTask.getParentId()).orElse(null);
        projectTask.setParent(parent);

    }

    @Transactional
    private Set<ProjectTask> changeLocationInner(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        if (!(dropLocation == GridDropLocation.ABOVE
                || dropLocation == GridDropLocation.BELOW
                || dropLocation == GridDropLocation.ON_TOP)) return new HashSet<>(0);

        if (dropLocation == GridDropLocation.ON_TOP && projectTasks.contains(target)) return new HashSet<>(0);

        List<ProjectTask> projectTaskList = new ArrayList<>(projectTasks);
        if (!projectTaskList.contains(target)) projectTaskList.add(target);

        validateVersion(projectTaskList);

        projectTaskList.remove(target);

        // Get levelOrder for moved tasks
        Data_LevelOrder_ChangedTasks data = getMovedTasksAndLevelOrder(projectTaskList, target, dropLocation);
        List<ProjectTask> movedTasksWithinParent = data.movedTasksWithinParent;
        int levelOrder = data.levelOrder;

        Map<ProjectTask, ProjectTask> parentsOfParentMap = new HashMap<>(0);
        if (!projectTaskList.isEmpty())
            parentsOfParentMap = hierarchyService.getParentsOfParent(target).stream()
                    .collect(Collectors.toMap(p -> p, p -> p));

        var parentIds = new HashSet<>();

        var newParentId = target.getParentId();
        if (dropLocation == GridDropLocation.ON_TOP) newParentId = target.getId();

        for (ProjectTask projectTask: projectTaskList) {
            if (parentsOfParentMap.get(projectTask) != null)
                throw new StandardError("The project structure has been changed. You should update the project and try again.");
            parentIds.add(projectTask.getParentId());
            projectTask.setParentId(newParentId);
            projectTask.setLevelOrder(levelOrder++);
        }

        var checkedIds = projectTasks.stream().map(ProjectTask::getId).collect(Collectors.toList());

        Set<ProjectTask> tasksWithChangedTerms;
        if (newParentId != null) {

            DependenciesSet dependenciesSet =
                    dependenciesService.getAllDependenciesWithCheckedChildren(newParentId, checkedIds);

            if (dependenciesSet.isCycle()) {
                dependenciesSet.fillWbs(this);
                String message = dependenciesService.getCycleLinkMessage(dependenciesSet);
                throw new StandardError(message);
            }
            tasksWithChangedTerms = recalculateTerms(dependenciesSet, parentIds);
        } else {
            tasksWithChangedTerms = recalculateTerms(parentIds);
        }

        projectTaskList.addAll(tasksWithChangedTerms);

        if (dropLocation == GridDropLocation.ABOVE) target.setLevelOrder(levelOrder++);

        if (!(dropLocation == GridDropLocation.ON_TOP)) {
            List<ProjectTask> afterTargetProjectTasks =
                    getTasksFollowingAfterTargetInBase(target.getId(), movedTasksWithinParent, levelOrder);
            projectTaskList.addAll(afterTargetProjectTasks);
        }

        projectTaskList.add(target);
        projectTaskList.addAll(movedTasksWithinParent);
        // if there are project task duplicates in projectTaskList, that something is wrong in dependenciesSet or
        // recalculateTerms or getTasksFollowingAfterTargetInBase. This situation has to additionally explore.
        projectTaskRepository.saveAll(projectTaskList);
        parentIds.add(newParentId);
        List<ProjectTask> savedElements = recalculateLevelOrderForChildrenOfProjectTaskIds(parentIds);
        projectTaskRepository.saveAll(savedElements);

        return projectTaskList.stream().filter(projectTasks::contains).collect(Collectors.toSet());

    }

    @Transactional
    private Set<ProjectTask> changedSortOrderTransactional(Set<ProjectTask> tasks, Direction direction) {

        validateVersion(tasks);

        List<?> ids = tasks.stream().map(ProjectTask::getId).toList();

        int directionNumber = 1;
        if (direction == Direction.UP) directionNumber = -1;

        List<ProjectTask> foundTasks = projectTaskRepository.findTasksThatFollowBeforeGivenTasks(ids, directionNumber);

        if (foundTasks.isEmpty()) return tasks;

        List<PropertiesPT> persistedTasks =
                foundTasks.stream()
                        .map(projectTask -> new PropertiesPT(projectTask, true))
                        .toList();

        List<PropertiesPT> currentTasks = tasks.stream()
                .map(projectTask -> new PropertiesPT(projectTask, false))
                .collect(toList());

        currentTasks.addAll(persistedTasks);
        if (direction == Direction.UP)
            currentTasks.sort(PropertiesPT::compareByPIDAndLevelOrder);
        else
            currentTasks.sort(PropertiesPT::compareByPIDAndLevelOrderReverse);;

        Object previousParent = null;
        PropertiesPT persistedElement = null;
        HashSet<ProjectTask> savedTasks = new HashSet<>(currentTasks.size());
        for (PropertiesPT pw: currentTasks) {
            ProjectTask projectTask = pw.value;
            if (!Objects.equals(projectTask.getParentId(), previousParent)) {
                previousParent = projectTask.getParentId();
                persistedElement = null;
            }
            if (pw.inBase) {
                persistedElement = pw;
                continue;
            }

            if (persistedElement == null) continue;

            int levelOrderOfCurrentTask = projectTask.getLevelOrder();
            ProjectTask previousTask = persistedElement.value;
            int levelOrderOfPreviousTask = previousTask.getLevelOrder();

            projectTask.setLevelOrder(levelOrderOfPreviousTask);
            previousTask.setLevelOrder(levelOrderOfCurrentTask);
            savedTasks.add(projectTask);
            savedTasks.add(previousTask);

        }

        projectTaskRepository.saveAll(savedTasks);

        savedTasks.retainAll(tasks);

        return savedTasks;

    }

    @AllArgsConstructor
    private static class PropertiesPT {
        ProjectTask value;
        boolean inBase;
        
        static int compareByPIDAndLevelOrder(PropertiesPT o1, PropertiesPT o2) {
            ProjectTask p1 = o1.value;
            ProjectTask p2 = o2.value;
            int parentId1 = p1.getParentId() == null ? p1.getNullId() : p1.getParentId();
            int parentId2 = p2.getParentId() == null ? p2.getNullId() : p2.getParentId();
            int result = Integer.compare(parentId1, parentId2);
            if (result != 0) return result;
            return Integer.compare(p1.getLevelOrder(), p2.getLevelOrder());    
        }

        static int compareByPIDAndLevelOrderReverse(PropertiesPT o1, PropertiesPT o2) {
            return compareByPIDAndLevelOrder(o2, o1);
        }

    }

    private Set<ProjectTask> getProjectTasksForSelection(Set<ProjectTask> projectTasks, Set<ProjectTask> changedTasks) {

        List<?> ids = changedTasks.stream().map(ProjectTask::getId).toList();

        Map<?, ProjectTask> projectTaskMap = getProjectTasksByIdWithFilledWbs(ids);

        for(ProjectTask projectTask: projectTasks) {
            ProjectTask foundedPT = projectTaskMap.getOrDefault(projectTask.getId(), null);
            if (foundedPT == null) continue;
            foundedPT.setChildrenCount(projectTask.getChildrenCount());
        }

        return new HashSet<>(projectTaskMap.values());

    }

    private void fillNecessaryFieldsIfItIsNew(ProjectTask projectTask) {

        if (!projectTask.isNew()) return;

        var parentId = projectTask.getParentId();

        if (parentId != null) {
            // Validate parent existence
            ProjectTask foundParent = projectTaskRepository.findById(parentId).orElse(null);
            if (foundParent == null) {
                projectTask.setParentId(null);
                parentId = null;
                //throw new StandardError("Unable to persist the object. A Parent of the task does not exist. Update the project and try again.");

            }
        }

        Integer levelOrder;
        if (parentId == null) {
            levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevelWhereParentNull();
        } else {
            levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parentId);
        }
        if (levelOrder == null) levelOrder = 0;
        projectTask.setLevelOrder(++levelOrder);

    }

    private void validateVersion(Collection<? extends ProjectTask> projectTasks) {

        List<?> ids = projectTasks.stream().map(ProjectTask::getId).toList();
        List<ProjectTask> projectTasksInBase = projectTaskRepository.findAllById(ids);
        Map<ProjectTask, ProjectTask> foundProjectTasks =
                projectTasksInBase.stream().collect(Collectors.toMap(p -> p, p -> p));

        if (projectTasks.size() != foundProjectTasks.size())
            throw new StandardError("Incorrect data. Should update the project and try again.");

        projectTasks.forEach(projectTask -> {
            ProjectTask projectTaskInBase = foundProjectTasks.getOrDefault(projectTask, null);
            if (projectTaskInBase == null || !projectTask.getVersion().equals(projectTaskInBase.getVersion()))
                throw new StandardError("The task " + projectTask + " has been changed by an another user. Should update the project and try again.");
        });

    }

    private Data_LevelOrder_ChangedTasks getMovedTasksAndLevelOrder(Collection<ProjectTask> projectTaskList,
                                                                    ProjectTask target, GridDropLocation dropLocation) {

        List<ProjectTask> movedTasksWithinParent = new ArrayList<>(0);
        Integer levelOrder;
        if (dropLocation == GridDropLocation.ABOVE || dropLocation == GridDropLocation.BELOW) {
            movedTasksWithinParent = projectTaskList.stream()
                            .filter(projectTask -> Objects.equals(projectTask.getParentId(), target.getParentId()))
                    .sorted(Comparator.comparingInt(ProjectTask::getLevelOrder)).collect(toList());

            if (!movedTasksWithinParent.isEmpty()) {
                projectTaskList.removeAll(movedTasksWithinParent);
            }
            levelOrder = target.getLevelOrder();
            if (dropLocation == GridDropLocation.BELOW) levelOrder++;

            for (ProjectTask projectTask: movedTasksWithinParent) {
                projectTask.setLevelOrder(levelOrder++);
            }

        } else {
            levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(target.getId());
            if (levelOrder == null) levelOrder = 0;
        }

        return new Data_LevelOrder_ChangedTasks(movedTasksWithinParent, levelOrder);

    }

    @AllArgsConstructor
    private static class Data_LevelOrder_ChangedTasks {
        List<ProjectTask> movedTasksWithinParent;
        Integer levelOrder;
    }

    private <I> List<ProjectTask> getTasksFollowingAfterTargetInBase(I targetId, List<ProjectTask> exceptedProjectTasks, Integer startLevelOrder) {

        List<?> ids = exceptedProjectTasks.stream().map(ProjectTask::getId).toList();

        List<ProjectTask> foundProjectTasks = projectTaskRepository.findTasksThatFollowAfterTargetWithoutExcludedTasks(targetId, ids);

        ArrayList<ProjectTask> savedTasks = new ArrayList<>(foundProjectTasks.size());
        int level_order = startLevelOrder;
        for (ProjectTask projectTask: foundProjectTasks) {
            if (level_order == projectTask.getLevelOrder()) {
                level_order++;
                continue;
            }
            projectTask.setLevelOrder(++level_order);
            savedTasks.add(projectTask);
        }

        savedTasks.trimToSize();

        return savedTasks;

    }

    private Set<ProjectTask> recalculateTerms(DependenciesSet dependenciesSet, Set<?> taskIds) {
        return new HashSet(0);
        // TODO recalculate terms
    }

    private Set<ProjectTask> recalculateTerms(Set<?> taskIds) {
        return new HashSet(0);
        // TODO recalculate terms
    }

    private Set<ProjectTask> recalculateTerms(ProjectTask projectTask) {
        // TODO recalculate terms
        // TODO getting dependencies and check changing terms
        //recalculateTerms(DependenciesSet dependenciesSet);
        return new HashSet(0);
    }

    private void populateListByRootItemRecursively(List<ProjectTask> projectTasks, TreeItem<ProjectTask> treeItem) {

        for (TreeItem<ProjectTask> child: treeItem.getChildren()) {
            projectTasks.add(child.getValue());
            populateListByRootItemRecursively(projectTasks, child);
        }

    }

    private List<ProjectTask> recalculateLevelOrderForChildrenOfProjectTaskIds(Collection<?> parentIds) {

        List<ProjectTask> foundProjectTasks;

        if (parentIds.stream().anyMatch(Objects::isNull)) {
            List<?> findingParentIds = parentIds.stream().filter(Objects::nonNull).collect(Collectors.toList());
            foundProjectTasks =
                    projectTaskRepository.findByParentIdInWithNullOrderByLevelOrderAsc(findingParentIds);
        } else {
            foundProjectTasks = projectTaskRepository.findByParentIdInOrderByLevelOrderAsc(parentIds);
        }

        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        return treeProjectTasks.recalculateLevelOrderForProjectTasks(foundProjectTasks);

    }

    private List<ProjectTask> getProjectTasksToDeletion(List<? extends ProjectTask> projectTasks) {

        projectTasks = projectTasks.stream().distinct().collect(Collectors.toList());

        if (projectTasks.size() == 0) {
            return new ArrayList<>();
        }

        List<ProjectTask> allHierarchyElements = hierarchyService.getElementsChildrenInDepth(projectTasks);

        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(allHierarchyElements);
        treeProjectTasks.fillWbs();
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
