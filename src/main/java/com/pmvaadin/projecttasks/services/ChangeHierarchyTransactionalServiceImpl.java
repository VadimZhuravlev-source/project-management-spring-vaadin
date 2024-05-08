package com.pmvaadin.projecttasks.services;

import com.pmvaadin.AppConfiguration;
import com.pmvaadin.common.tree.TreeItem;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.projectstructure.TreeProjectTasksImpl;
import com.pmvaadin.projecttasks.dependencies.DependenciesService;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.HierarchyElement;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskOrderedHierarchy;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.security.entities.User;
import com.pmvaadin.security.services.UserService;
import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.terms.calculation.TermCalculationRespond;
import com.pmvaadin.terms.calculation.TermCalculationRespondImpl;
import com.pmvaadin.terms.calculation.TermsCalculation;
import com.pmvaadin.terms.calendars.services.TermCalculationService;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class ChangeHierarchyTransactionalServiceImpl implements ChangeHierarchyTransactionalService {

    private ProjectTaskRepository projectTaskRepository;
    private HierarchyService hierarchyService;
    private DependenciesService dependenciesService;
    private TermCalculationService termCalculationService;
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository){
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setHierarchyService(HierarchyService hierarchyService){
        this.hierarchyService = hierarchyService;
    }

    @Autowired
    public void setDependenciesService(DependenciesService dependenciesService){
        this.dependenciesService = dependenciesService;
    }

    @Autowired
    public void setTermCalculationService(TermCalculationService calendarService){
        this.termCalculationService = calendarService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public TermCalculationRespond moveTasksInHierarchy(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        var taskIdsForRecalculate = changeHierarchy(projectTasks, target, dropLocation);

        var respond = recalculateTerms(taskIdsForRecalculate);

        List<ProjectTask> savedElements = recalculateLevelOrderByParentIds(taskIdsForRecalculate);
        projectTaskRepository.saveAll(savedElements);

        return respond;

    }

    @Override
    @Transactional
    public TermCalculationRespond moveTasksInHierarchy(Set<ProjectTask> projectTasks, ProjectTreeService.Direction direction) {

        validateVersion(projectTasks);

        if (direction == ProjectTreeService.Direction.UP)
            return moveTasksUpHierarchy(projectTasks);
        else
            return moveTasksDownHierarchy(projectTasks);

    }

    @Override
    @Transactional
    public TermCalculationRespond recalculateTerms(EntityManager entityManager, Set<?> taskIds) {

        var newTaskIds = taskIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        if (newTaskIds.isEmpty()) return TermCalculationRespondImpl.getEmptyInstance();

        entityManager.flush();
        TermCalculationData termCalculationData = dependenciesService.getAllDependenciesForTermCalc(entityManager, newTaskIds);

        termCalculationService.fillCalendars(termCalculationData);

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
        TermsCalculation termsCalculation = context.getBean(TermsCalculation.class);
        TermCalculationRespond respond = termsCalculation.calculate(termCalculationData);

        projectTaskRepository.saveAll(respond.getChangedTasks());

        //projectRecalculation.recalculate(respond.getRecalculatedProjects());

        return respond;

    }

    @Override
    public void recalculateTerms(ProjectTask projectTask) {
        recalculateTermsProjectTask(projectTask);
    }

    @Override
    public Map<?, ProjectTask> getProjectTasksByIdWithFilledWbs(Collection<?> ids) {

        if (ids.size() == 0) return new HashMap<>();

        ids = ids.stream().distinct().toList();

        var projectTasks = hierarchyService.getParentsOfParent(ids);
        var treeProjectTasks = new TreeProjectTasksImpl();
        treeProjectTasks.populateTreeByList(projectTasks);
        treeProjectTasks.fillWbs();
        Map<?, ?> filter = ids.stream().collect(Collectors.toMap(id -> id, id -> false));
        return projectTasks.stream().
                filter(projectTask -> filter.containsKey(projectTask.getId())).
                collect(Collectors.toMap(ProjectTask::getId, p -> p));

    }

    @Override
    @Transactional
    public void changeSortOrder(Set<ProjectTask> projectTasks, ProjectTreeService.Direction direction) {
        changedSortOrderPrivateMethod(projectTasks, direction);
    }

    @Override
    @Transactional
    public TermCalculationRespond delete(List<? extends ProjectTask> projectTasks) {

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

        var parentIdsSet = new HashSet<>(parentIds);
        return recalculateTerms(this.entityManager, parentIdsSet);

    }

    private TermCalculationRespond moveTasksUpHierarchy(Set<ProjectTask> projectTasks) {

        List<ProjectTask> changeableTasks = projectTasks.stream().filter(p -> p.getParentId() != null)
                .collect(toList());

        var accessChecker = new AccessChecker();
        accessChecker.deleteTasksForTasksMovingUp(changeableTasks);

        if (changeableTasks.isEmpty()) return TermCalculationRespondImpl.getEmptyInstance();

        var parentIds = changeableTasks.stream()
                .map(ProjectTask::getParentId).distinct()
                .toList();

        List<ProjectTask> tasksThatFollowAfterParents = projectTaskRepository.findTasksThatFollowAfterGivenTasks(parentIds);

        if (tasksThatFollowAfterParents.isEmpty()) return TermCalculationRespondImpl.getEmptyInstance();

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

                    if (movedTask.getParentId() != null) taskIdsForRecalculateTerm.add(movedTask.getParentId());
                    movedTask.setParentId(task.getParentId());
                    movedTask.setLevelOrder(currentLevelOrder + ++changeMagnitude);
                    savedTasks.add(movedTask);

                }
                if (task.getParentId() != null) taskIdsForRecalculateTerm.add(task.getParentId());
            }

        }

        projectTaskRepository.saveAll(savedTasks);

        // the method below uses also as a check of cycle dependency
        var respond = recalculateTerms(taskIdsForRecalculateTerm);

        List<ProjectTask> recalculatedTasks = recalculateLevelOrderByParentIds(parentIds);
        projectTaskRepository.saveAll(recalculatedTasks);

        return respond;
    }

    private TermCalculationRespond moveTasksDownHierarchy(Set<ProjectTask> tasks) {

        List<?> ids = tasks.stream().map(ProjectTask::getId).toList();

        int directionNumber = -1;

        List<ProjectTask> foundTasks = projectTaskRepository.findTasksThatFollowToGivenDirection(ids, directionNumber);

        if (foundTasks.isEmpty()) return TermCalculationRespondImpl.getEmptyInstance();

        List<PropertiesPT> persistedTasks =
                foundTasks.stream()
                        .map(projectTask -> new PropertiesPT(projectTask, true))
                        .toList();

        List<PropertiesPT> currentTasks = tasks.stream()
                .map(projectTask -> new PropertiesPT(projectTask, false))
                .collect(toList());

        currentTasks.addAll(persistedTasks);

        // There has caused an error in the grouping below if at least one value is null, so fill in by min of Integer
        currentTasks.forEach(propertiesPT -> propertiesPT.value.setUniqueValueIfParentIdNull());

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

        // Take back previous values to parent_id
        currentTasks.forEach(propertiesPT -> propertiesPT.value.revertParentIdNull());

        return changeLocation(newParentsOfMovedTasks);

    }

    private TermCalculationRespond changeLocation(Map<ProjectTask, List<ProjectTask>> newParentsOfMovedTasks) {

        // TODO compose additional method changeLocation for a check out of all dependencies as
        //  there is accomplish heavy query in the changeLocationInner method
        var taskIdsForRecalculate = new HashSet<>();
        for (Map.Entry<ProjectTask, List<ProjectTask>> kV: newParentsOfMovedTasks.entrySet()) {
            ProjectTask newParent = kV.getKey();
            List<ProjectTask> movedTasks = kV.getValue();
            Set<?> modifiedTaskIds = changeHierarchy(movedTasks, newParent, GridDropLocation.ON_TOP);
            taskIdsForRecalculate.addAll(modifiedTaskIds);
        }

        var respond = recalculateTerms(taskIdsForRecalculate);

        List<ProjectTask> savedElements = recalculateLevelOrderByParentIds(taskIdsForRecalculate);
        projectTaskRepository.saveAll(savedElements);

        return respond;

    }

    private Set<?> changeHierarchy(Collection<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        var hierarchyChanger = new HierarchyChanger(this);

        return hierarchyChanger.changeHierarchy(projectTasks, target, dropLocation);

    }

    private void changedSortOrderPrivateMethod(Set<ProjectTask> projectTasks, ProjectTreeService.Direction direction) {

        var tasks = new HashSet<>(projectTasks);

        validateVersion(tasks);

        var accessChecker = new AccessChecker();
        accessChecker.deleteTasksForSortOrderChanger(tasks);

        List<?> ids = tasks.stream().map(ProjectTask::getId).toList();

        int directionNumber = 1;
        if (direction == ProjectTreeService.Direction.UP) directionNumber = -1;

        List<ProjectTask> foundTasks = projectTaskRepository.findTasksThatFollowToGivenDirection(ids, directionNumber);

        if (foundTasks.isEmpty()) return;

        List<PropertiesPT> persistedTasks =
                foundTasks.stream()
                        .map(projectTask -> new PropertiesPT(projectTask, true))
                        .toList();

        List<PropertiesPT> currentTasks = tasks.stream()
                .map(projectTask -> new PropertiesPT(projectTask, false))
                .collect(toList());

        currentTasks.addAll(persistedTasks);

        if (direction == ProjectTreeService.Direction.UP)
            currentTasks.sort(PropertiesPT::compareByPIDAndLevelOrder);
        else
            currentTasks.sort(PropertiesPT::compareByPIDAndLevelOrderReverse);

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

//        savedTasks.retainAll(tasks);

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

    private List<ProjectTask> recalculateLevelOrderByParentIds(Collection<?> parentIds) {

        List<ProjectTask> foundProjectTasks;

        if (parentIds.stream().anyMatch(Objects::isNull)) {
            List<?> findingParentIds = parentIds.stream().filter(Objects::nonNull).collect(Collectors.toList());
            foundProjectTasks =
                    projectTaskRepository.findByParentIdInWithNullOrderByLevelOrderAsc(findingParentIds);
        } else {
            foundProjectTasks = projectTaskRepository.findByParentIdInOrderByLevelOrderAsc(parentIds);
        }

        var treeProjectTasks = new TreeProjectTasksImpl();
        return treeProjectTasks.recalculateLevelOrderForProjectTasks(foundProjectTasks);

    }

    private List<ProjectTask> getProjectTasksToDeletion(List<? extends ProjectTask> projectTasks) {

        projectTasks = projectTasks.stream().distinct().collect(Collectors.toList());

        if (projectTasks.size() == 0) {
            return new ArrayList<>();
        }

        List<ProjectTask> allHierarchyElements = hierarchyService.getElementsChildrenInDepth(projectTasks);
        TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
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

    private TermCalculationRespond recalculateTerms(Set<?> taskIds) {

        return recalculateTerms(this.entityManager, taskIds);

    }

    private void recalculateTermsProjectTask(ProjectTask projectTask) {
        HashSet<Object> ids = new HashSet<>(1);
        ids.add(projectTask.getId());
        recalculateTerms(ids);
    }

    private boolean isIllegalRight(UserService.AccessRights accessRights) {
        return !legalRight(accessRights);
    }

    private boolean legalRight(UserService.AccessRights accessRights) {
        return accessRights.isOneOfAllowedChildren()
                || accessRights.isChildOfRoot() && accessRights.isCurrentTaskAnAllowedProject();
    }

    private class AccessChecker {

        private void deleteTasksForSortOrderChanger(Set<ProjectTask> projectTasks) {
            deleteTasksUserDoesNotHaveRightsForChange(projectTasks,
                    ChangeHierarchyTransactionalServiceImpl.this::isIllegalRight);
        }

        private void deleteTasksForTasksMovingUp(List<ProjectTask> projectTasks) {
            deleteTasksUserDoesNotHaveRightsForChange(projectTasks,
                    accessRights -> !accessRights.isOneOfAllowedChildren());
        }

        // functionality of this method particularly intersects with method HierarchyChanger.deleteTasksUserDoesNotHaveRightsForChange
        private void deleteTasksUserDoesNotHaveRightsForChange(Collection<ProjectTask> projectTasks,
                                                               Predicate<UserService.AccessRights> rightChecker) {

            if (projectTasks.isEmpty())
                return;

            var user = userService.getCurrentUser();
            if (!userService.isUserFollowingRLS(user)) {
                return;
            }

            var checkingIds = projectTasks.stream().map(ProjectTask::getId).collect(Collectors.toSet());
            var parents = hierarchyService.getParentsOfParent(checkingIds);
            var accessTable = userService.getUserAccessTable(projectTasks, user, parents);

            var deletingTasks = projectTasks.stream().filter(task -> {
                var accessRights = accessTable.get(task.getId());
                return rightChecker.test(accessRights);
            }).toList();

            projectTasks.removeAll(deletingTasks);

        }

    }

    private class HierarchyChanger {

        private final ChangeHierarchyTransactionalService projectTaskService;
        private List<ProjectTask> projectTasks;

        private List<ProjectTask> movedTasksWithinParent;
        private int levelOrder;
        private User user;
        private boolean isUserFollowingRLS;
        private List<ProjectTask> parents;
        private final List<ProjectTask> newUsersProjects = new ArrayList<>();
        private Map<?, UserService.AccessRights> accessTable;

        HierarchyChanger(ChangeHierarchyTransactionalService projectTaskService) {
            this.projectTaskService = projectTaskService;
        }

        private Set<?> changeHierarchy(Collection<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

            if (!(dropLocation == GridDropLocation.ABOVE
                    || dropLocation == GridDropLocation.BELOW
                    || dropLocation == GridDropLocation.ON_TOP))
                return new HashSet<>(0);

            boolean targetIsInProjectTasks = projectTasks.contains(target);
            if (dropLocation == GridDropLocation.ON_TOP && targetIsInProjectTasks)
                return new HashSet<>(0);

            this.projectTasks = new ArrayList<>(projectTasks);
            if (!targetIsInProjectTasks)
                this.projectTasks.add(target);

            validateVersion(this.projectTasks);

            // TODO validation links type of the summary task "target"

            this.projectTasks.remove(target);

            var newParentId = target.getParentId();
            if (dropLocation == GridDropLocation.ON_TOP)
                newParentId = target.getId();
            this.user = userService.getCurrentUser();
            isUserFollowingRLS = userService.isUserFollowingRLS(user);
            if (isUserFollowingRLS && newParentId == null && user.getRootProjectId() != null)
                newParentId = user.getRootProjectId();

            fillPrincipalFields(newParentId);

            deleteTasksUserDoesNotHaveRightsForChange(newParentId);

            if (this.projectTasks.isEmpty())
                return new HashSet<>(0);

            setLockOnParentsOfTarget(newParentId);
            fillNewUsersProjects(newParentId);

            fillMovedTasksAndLevelOrder(this.projectTasks, target, dropLocation);

            var checkedIds = this.projectTasks.stream().map(ProjectTask::getId).collect(toList());

            if (newParentId != null) {

                DependenciesSet dependenciesSet =
                        dependenciesService.getAllDependenciesWithCheckedChildren(newParentId, checkedIds);

                if (dependenciesSet.isCycle()) {
                    dependenciesSet.fillWbs(projectTaskService);
                    String message = dependenciesService.getCycleLinkMessage(dependenciesSet);
                    throw new StandardError(message);
                }

            }

            var parentIdsForRecalculation = new HashSet<>();
            for (ProjectTask projectTask: this.projectTasks) {
                if (projectTask.getParentId() != null) {
                    parentIdsForRecalculation.add(projectTask.getParentId());
                }
                projectTask.setParentId(newParentId);
                projectTask.setLevelOrder(levelOrder++);
            }

            if (dropLocation == GridDropLocation.ABOVE) {
                target.setLevelOrder(levelOrder++);
                this.projectTasks.add(target);
            }

            if (!(dropLocation == GridDropLocation.ON_TOP)) {
                List<ProjectTask> afterTargetProjectTasks =
                        getTasksFollowingAfterTargetInBase(target.getId(), movedTasksWithinParent, levelOrder);
                this.projectTasks.addAll(afterTargetProjectTasks);
            }

            this.projectTasks.addAll(movedTasksWithinParent);
            // if there are project task duplicates in projectTaskList, that something has gone wrong in dependenciesSet or
            // recalculateTerms or getTasksFollowingAfterTargetInBase. This situation has to additionally explore.
            // TODO PersistenceException handler
            projectTaskRepository.saveAll(this.projectTasks);
            //entityManager.flush();

            if (isUserFollowingRLS && !this.newUsersProjects.isEmpty())
                userService.addProjectTaskToUserProject(this.newUsersProjects, this.user);

            parentIdsForRecalculation.add(newParentId);

            return parentIdsForRecalculation;

        }

        private void fillPrincipalFields(Object targetId) {

            if (!isUserFollowingRLS) {
                if (targetId == null) {
                    this.parents = new ArrayList<>(0);
                    return;
                }
                var listOfTarget = new ArrayList<>(1);
                listOfTarget.add(targetId);
                this.parents = hierarchyService.getParentsOfParent(listOfTarget);
                return;
            }

            Set<Object> checkingIds = this.projectTasks.stream().map(ProjectTask::getId).collect(Collectors.toSet());
            if (targetId != null) {
                checkingIds.add(targetId);
            }

            this.parents = hierarchyService.getParentsOfParent(checkingIds);

        }

        private void fillNewUsersProjects(Object targetId) {

            if (!isUserFollowingRLS)
                return;

            var targetRights = accessTable.get(targetId);
            if (targetRights.isOneOfAllowedChildren() || targetRights.isCurrentTaskAnAllowedProject())
                return;

            for (var projectTask: this.projectTasks) {
                var accessRight = accessTable.get(projectTask.getId());
                if (!accessRight.isCurrentTaskAnAllowedProject())
                    newUsersProjects.add(projectTask);
            }

        }

        private void deleteTasksUserDoesNotHaveRightsForChange(Object targetId) {

            if (!isUserFollowingRLS)
                return;

            //this.projectTasks.stream().map(e -> (HierarchyElement) e).collect(toList());
            List<HierarchyElement<Object>> elements = new ArrayList<>(this.projectTasks.size() + 1);
            for (var projectTask: this.projectTasks) {
                elements.add((HierarchyElement) projectTask);
            }

            if (targetId != null) {
                var parentOpt = this.parents.stream().filter(projectTask -> projectTask.getId().equals(targetId)).findFirst();
                parentOpt.ifPresent(projectTask -> elements.add((HierarchyElement) projectTask));
            }
            this.accessTable = userService.getUserAccessTable(elements, user, parents);

            throwExceptionIfTargetHasIllegalAccessRights(accessTable, targetId);

            var deletingTasks = projectTasks.stream().filter(task -> {
                var accessRights = accessTable.get(task.getId());
                return isIllegalRight(accessRights);
            }).toList();

            projectTasks.removeAll(deletingTasks);

        }

        private void throwExceptionIfTargetHasIllegalAccessRights(Map<?, UserService.AccessRights> accessTable, Object targetId) {
            if (targetId == null)
                return;
            var accessRights = accessTable.get(targetId);
            if (isIllegalRight(accessRights) && !accessRights.isRootProject()) {
                throw new StandardError(getTextOfIllegalChange());
            }
        }

        private String getTextOfIllegalChange() {
            return "An illegal change. It is not possible to move tasks in this way.";
        }

        private void setLockOnParentsOfTarget(Object targetId) {

            if (targetId == null)
                return;

            var lockingTaskIds = new HashSet<>();

            var parentsMap = parents.stream().collect(Collectors.toMap(ProjectTask::getId, p -> p));
            var parent = parentsMap.get(targetId);
            while (parent != null) {
                lockingTaskIds.add(parent.getId());
                parent = parentsMap.get(parent.getParentId());
            }

            var query = entityManager.createQuery("FROM ProjectTaskImpl WHERE id IN(:ids)");
            query.setParameter("ids", lockingTaskIds);
            query.setLockMode(LockModeType.PESSIMISTIC_READ);
            query.getResultList();

        }

        private void fillMovedTasksAndLevelOrder(Collection<ProjectTask> projectTaskList,
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

            this.levelOrder = levelOrder;
            this.movedTasksWithinParent = movedTasksWithinParent;

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

    }

}
