package com.pmvaadin.projecttasks.services;

import com.pmvaadin.AppConfiguration;
import com.pmvaadin.calendars.services.CalendarService;
import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projectstructure.TestCase;
import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.projectstructure.termscalculation.TermCalculationData;
import com.pmvaadin.projectstructure.termscalculation.TermCalculationRespond;
import com.pmvaadin.projectstructure.termscalculation.TermsCalculation;
import com.pmvaadin.projecttasks.dependencies.DependenciesService;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskOrderedHierarchy;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
    private CalendarService calendarService;

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

    @Autowired
    public void setCalendarService(CalendarService calendarService){
        this.calendarService = calendarService;
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
    @Transactional
    public ProjectTask save(ProjectTask projectTask, boolean validate, boolean recalculateTerms) {

        if (validate && !validate(projectTask)) return projectTask;
        fillNecessaryFieldsIfItIsNew(projectTask);
        ProjectTask savedProjectTask = projectTaskRepository.save(projectTask);
        if (!recalculateTerms) {
            return savedProjectTask;
        }
        Set<ProjectTask> tasks = recalculateTerms(savedProjectTask);
        projectTaskRepository.saveAll(tasks);
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
        List<ProjectTask> savedElements = recalculateLevelOrderByParentIds(parentIds);
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
    @Transactional
    public Set<ProjectTask> changeLocation(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        Set<ProjectTask> changedTasks = changeLocationInner(projectTasks, target, dropLocation);

        return getProjectTasksForSelection(projectTasks, changedTasks);

    }

    @Override
    @Transactional
    public Set<ProjectTask> changeLocation(Set<ProjectTask> projectTasks, Direction direction) {

        Set<ProjectTask> changedTasks = changeLocationInner(projectTasks, direction);

        return getProjectTasksForSelection(projectTasks, changedTasks);

    }

    @Override
    @Transactional
    public Set<ProjectTask> changeSortOrder(Set<ProjectTask> projectTasks, Direction direction) {

        Set<ProjectTask> changedTasks = changedSortOrderInner(projectTasks, direction);

        return getProjectTasksForSelection(projectTasks, changedTasks);

    }

    @Override
    public ProjectTask sync(ProjectTask projectTask) {
        return projectTaskRepository.findById(projectTask.getId()).orElse(null);
    }

    @Override
    public Map<?, ProjectTask> getProjectTasksByIdWithFilledWbs(Collection<?> ids) {

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

    private Set<ProjectTask> changeLocationInner(Set<ProjectTask> projectTasks, Direction direction) {

        validateVersion(projectTasks);

        if (direction == Direction.UP)
            return changeLocationUp(projectTasks);
        else
            return changeLocationDown(projectTasks);

    }

    private Set<ProjectTask> changeLocationInner(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        var taskIdsForRecalculate = changeHierarchy(projectTasks, target, dropLocation);

        Set<ProjectTask> tasksWithChangedTerms = recalculateTerms(taskIdsForRecalculate);
        projectTaskRepository.saveAll(tasksWithChangedTerms);

        List<ProjectTask> savedElements = recalculateLevelOrderByParentIds(taskIdsForRecalculate);
        projectTaskRepository.saveAll(savedElements);

        return projectTasks;

    }

    private Set<ProjectTask> changeLocationUp(Set<ProjectTask> projectTasks) {

        List<ProjectTask> changeableTasks = projectTasks.stream().filter(p -> p.getParentId() != null)
                .toList();

        if (changeableTasks.isEmpty()) return new HashSet<>(0);

        var parentIds = changeableTasks.stream()
                .map(ProjectTask::getParentId).distinct()
                .toList();

        List<ProjectTask> tasksThatFollowAfterParents = projectTaskRepository.findTasksThatFollowAfterGivenTasks(parentIds);

        if (tasksThatFollowAfterParents.isEmpty()) return new HashSet<>(0);

        Map<?, List<ProjectTask>> groupedByParentId = changeableTasks.stream().collect(groupingBy(ProjectTask::getParentId));

        Object previousParentId = tasksThatFollowAfterParents.get(0).getParentId();
        int changeMagnitude = 0;
        ArrayList<ProjectTask> savedTasks = new ArrayList<>(tasksThatFollowAfterParents.size() + changeableTasks.size());
        HashSet<Object> taskIdsForRecalculateTerm = new HashSet<>(parentIds);
        for (ProjectTask task: tasksThatFollowAfterParents) {
            if (!Objects.equals(task.getParentId(), previousParentId)) {
                changeMagnitude = 0;
                previousParentId = task.getParentId();
            }

            int currentLevelOrder = task.getLevelOrder();
            if (changeMagnitude != 0) {
                task.setLevelOrder(currentLevelOrder + changeMagnitude);
                savedTasks.add(task);
            }
            List<ProjectTask> movedTasks = groupedByParentId.getOrDefault(task.getId(), null);
            if (movedTasks != null) {
                for (ProjectTask movedTask: movedTasks) {

                    taskIdsForRecalculateTerm.add(movedTask.getParentId());
                    movedTask.setParentId(task.getParentId());
                    movedTask.setLevelOrder(currentLevelOrder + ++changeMagnitude);
                    savedTasks.add(movedTask);

                }
                taskIdsForRecalculateTerm.add(task.getParentId());
            }

        }

        projectTaskRepository.saveAll(savedTasks);

        Set<ProjectTask> tasksWithChangedTerms = recalculateTerms(taskIdsForRecalculateTerm);
        projectTaskRepository.saveAll(tasksWithChangedTerms);

        List<ProjectTask> recalculatedTasks = recalculateLevelOrderByParentIds(parentIds);
        projectTaskRepository.saveAll(recalculatedTasks);

        return projectTasks;
    }

    private Set<ProjectTask> changeLocationDown(Set<ProjectTask> tasks) {

        List<?> ids = tasks.stream().map(ProjectTask::getId).toList();

        int directionNumber = -1;

        List<ProjectTask> foundTasks = projectTaskRepository.findTasksThatFollowToGivenDirection(ids, directionNumber);

        if (foundTasks.isEmpty()) return tasks;

        List<PropertiesPT> persistedTasks =
                foundTasks.stream()
                        .map(projectTask -> new PropertiesPT(projectTask, true))
                        .toList();

        List<PropertiesPT> currentTasks = tasks.stream()
                .map(projectTask -> new PropertiesPT(projectTask, false))
                .collect(toList());

        currentTasks.addAll(persistedTasks);

        Map<?, List<PropertiesPT>> groupedByParentId = currentTasks.stream().collect(groupingBy(p -> p.value.getParentId()));

        Map<ProjectTask, List<ProjectTask>> newParentsOfMovedTasks = new HashMap<>();

        for (Map.Entry<?, List<PropertiesPT>> mapEntry: groupedByParentId.entrySet()) {

            List<PropertiesPT> ptList = mapEntry.getValue();

            ptList.sort(Comparator.comparingInt(o -> o.value.getLevelOrder()));

            ProjectTask previousPT = null;

            for (PropertiesPT prop: ptList) {

                ProjectTask projectTask = prop.value;
                if (prop.inBase) {
                    previousPT = projectTask;
                    newParentsOfMovedTasks.put(projectTask, new ArrayList<>());
                    continue;
                }

                if (previousPT == null) continue;

                List<ProjectTask> movedTasks = newParentsOfMovedTasks.getOrDefault(previousPT, null);

                if (movedTasks == null) continue;

                movedTasks.add(projectTask);

            }

        }

        changeLocation(newParentsOfMovedTasks);

        return tasks;
    }

    private void changeLocation(Map<ProjectTask, List<ProjectTask>> newParentsOfMovedTasks) {

        // TODO compose additional method changeLocation for a check out of all dependencies as
        //  there is accomplish heavy query in the changeLocationInner method
        var taskIdsForRecalculate = new HashSet<>();
        for (Map.Entry<ProjectTask, List<ProjectTask>> kV: newParentsOfMovedTasks.entrySet()) {
            ProjectTask newParent = kV.getKey();
            List<ProjectTask> movedTasks = kV.getValue();
            Set<?> modifiedTaskIds = changeHierarchy(movedTasks, newParent, GridDropLocation.ON_TOP);
            taskIdsForRecalculate.addAll(modifiedTaskIds);
        }

        Set<ProjectTask> tasksWithChangedTerms = recalculateTerms(taskIdsForRecalculate);
        projectTaskRepository.saveAll(tasksWithChangedTerms);

        List<ProjectTask> savedElements = recalculateLevelOrderByParentIds(taskIdsForRecalculate);
        projectTaskRepository.saveAll(savedElements);

    }

    private Set<?> changeHierarchy(Collection<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        if (!(dropLocation == GridDropLocation.ABOVE
                || dropLocation == GridDropLocation.BELOW
                || dropLocation == GridDropLocation.ON_TOP)) return new HashSet<>(0);

        boolean targetIsInProjectTasks = projectTasks.contains(target);
        if (dropLocation == GridDropLocation.ON_TOP && targetIsInProjectTasks) return new HashSet<>(0);

        List<ProjectTask> projectTaskList = new ArrayList<>(projectTasks);
        if (!targetIsInProjectTasks) projectTaskList.add(target);

        validateVersion(projectTaskList);

        // TODO validation type links with summary task "target"

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

        if (newParentId != null) {

            DependenciesSet dependenciesSet =
                    dependenciesService.getAllDependenciesWithCheckedChildren(newParentId, checkedIds);

            if (dependenciesSet.isCycle()) {
                dependenciesSet.fillWbs(this);
                String message = dependenciesService.getCycleLinkMessage(dependenciesSet);
                throw new StandardError(message);
            }
        }

        if (dropLocation == GridDropLocation.ABOVE) {
            target.setLevelOrder(levelOrder++);
            projectTaskList.add(target);
        }

        if (!(dropLocation == GridDropLocation.ON_TOP)) {
            List<ProjectTask> afterTargetProjectTasks =
                    getTasksFollowingAfterTargetInBase(target.getId(), movedTasksWithinParent, levelOrder);
            projectTaskList.addAll(afterTargetProjectTasks);
        }

        projectTaskList.addAll(movedTasksWithinParent);
        // if there are project task duplicates in projectTaskList, that something has gone wrong in dependenciesSet or
        // recalculateTerms or getTasksFollowingAfterTargetInBase. This situation has to additionally explore.
        projectTaskRepository.saveAll(projectTaskList);

        parentIds.add(newParentId);

        return parentIds;

    }

    private Set<ProjectTask> changedSortOrderInner(Set<ProjectTask> tasks, Direction direction) {

        validateVersion(tasks);

        List<?> ids = tasks.stream().map(ProjectTask::getId).toList();

        int directionNumber = 1;
        if (direction == Direction.UP) directionNumber = -1;

        List<ProjectTask> foundTasks = projectTaskRepository.findTasksThatFollowToGivenDirection(ids, directionNumber);

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

        Object previousParent = currentTasks.get(0);
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

    private Set<ProjectTask> recalculateTerms(Set<?> taskIds) {
        //return new HashSet(0);

        TermCalculationData termCalculationData = dependenciesService.getAllDependenciesForTermCalc(taskIds);

        calendarService.fillCalendars(termCalculationData);

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
        TermsCalculation termsCalculation = context.getBean(TermsCalculation.class);
        TermCalculationRespond respond = termsCalculation.calculate(termCalculationData);

        return respond.getChangedTasks();

    }

    private Set<ProjectTask> recalculateTerms(ProjectTask projectTask) {
        HashSet<Object> parentIds = new HashSet<>(1);
        parentIds.add(projectTask.getParentId());
        return recalculateTerms(parentIds);
    }

    private void populateListByRootItemRecursively(List<ProjectTask> projectTasks, TreeItem<ProjectTask> treeItem) {

        for (TreeItem<ProjectTask> child: treeItem.getChildren()) {
            projectTasks.add(child.getValue());
            populateListByRootItemRecursively(projectTasks, child);
        }

    }

    private List<ProjectTask> recalculateLevelOrderByParentIds(Collection<?> parentIds) {

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
