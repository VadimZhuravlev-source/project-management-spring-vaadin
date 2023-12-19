package com.pmvaadin.projecttasks.services;

import com.pmvaadin.AppConfiguration;
import com.pmvaadin.projectstructure.ProjectRecalculation;
import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projectstructure.TestCase;
import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.terms.calculation.TermCalculationRespond;
import com.pmvaadin.terms.calculation.TermCalculationRespondImpl;
import com.pmvaadin.terms.calculation.TermsCalculation;
import com.pmvaadin.projecttasks.dependencies.DependenciesService;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskOrderedHierarchy;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.terms.calendars.services.TermCalculationService;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private ProjectTaskRepository projectTaskRepository;
    private HierarchyService hierarchyService;
    private TreeProjectTasks treeProjectTasks;
    private ProjectRecalculation projectRecalculation;
    private ChangeHierarchyTransactionalService changeHierarchyTransactionalService;

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
    public void setTreeProjectTasks(TreeProjectTasks treeProjectTasks){
        this.treeProjectTasks = treeProjectTasks;
    }

    @Autowired
    public void setProjectRecalculation(ProjectRecalculation projectRecalculation){
        this.projectRecalculation = projectRecalculation;
    }

    @Autowired
    public void setChangeHierarchyTransactionalService(ChangeHierarchyTransactionalService changeHierarchyTransactionalService) {
        this.changeHierarchyTransactionalService = changeHierarchyTransactionalService;
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
        var respond = changeHierarchyTransactionalService.recalculateTerms(savedProjectTask);
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
//    @Transactional
    public void delete(List<? extends ProjectTask> projectTasks) {

        List<?> parentIds = projectTasks.stream().map(ProjectTask::getParentId).toList();
        List<ProjectTask> projectTaskForDeletion = getProjectTasksToDeletion(projectTasks);
        Map<?, Boolean> parentIdsForDeletion =
                projectTaskForDeletion.stream().collect(Collectors.toMap(ProjectTask::getId, projectTask -> true));
        parentIds = parentIds.stream().filter(parentId -> Objects.isNull(parentIdsForDeletion.get(parentId))).collect(Collectors.toList());
        List<?> projectTaskIds = projectTaskForDeletion.stream()
                //.sorted(Comparator.comparing(ProjectTaskOrderedHierarchy::getWbs).reversed())
                .map(ProjectTask::getId).toList();

        projectTaskRepository.deleteAllById(projectTaskIds);
        List<ProjectTask> savedElements = recalculateLevelOrderByParentIds(parentIds);
        projectTaskRepository.saveAll(savedElements);

        changeHierarchyTransactionalService.recalculateTerms(entityManager, new HashSet<>(parentIds));

    }

    @Override
    public void recalculateProject() {

//        List<ProjectTask> projectTasks = projectTaskRepository.findAllByOrderByLevelOrderAsc();
//        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
//        treeProjectTasks.populateTreeByList(projectTasks);
//        List<ProjectTask> savedElements = treeProjectTasks.recalculateThePropertiesOfTheWholeProject();
//        projectTaskRepository.saveAll(savedElements);

    }

    @Override
    //@Transactional
    public void changeLocation(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        var respond = changeHierarchyTransactionalService.changeLocation(projectTasks, target, dropLocation);
        this.projectRecalculation.recalculate(respond.getRecalculatedProjects());

    }

    @Override
    //@Transactional
    public void changeLocation(Set<ProjectTask> projectTasks, Direction direction) {

        var respond = changeHierarchyTransactionalService.changeLocation(projectTasks, direction);
        this.projectRecalculation.recalculate(respond.getRecalculatedProjects());

    }

    @Override
    @Transactional
    public void changeSortOrder(Set<ProjectTask> projectTasks, Direction direction) {

        Set<ProjectTask> changedTasks = changedSortOrderInner(projectTasks, direction);

        //return getProjectTasksForSelection(projectTasks, changedTasks);

    }

    @Override
    public ProjectTask sync(ProjectTask projectTask) {
        return projectTaskRepository.findById(projectTask.getId()).orElse(null);
    }

    @Override
    public Map<?, ProjectTask> getProjectTasksByIdWithFilledWbs(Collection<?> ids) {

        return changeHierarchyTransactionalService.getProjectTasksByIdWithFilledWbs(ids);

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

    @Override
    public TermCalculationRespond recalculateTerms(EntityManager entityManager, Set<?> taskIds) {

        return changeHierarchyTransactionalService.recalculateTerms(entityManager, taskIds);

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
        return allHierarchyElements;
        //TreeProjectTasks treeProjectTasks = new TreeProjectTasksImpl();
//        treeProjectTasks.populateTreeByList(allHierarchyElements);
//        treeProjectTasks.fillWbs();
//        TreeItem<ProjectTask> rootItem = treeProjectTasks.getRootItem();

//        List<ProjectTask> projectTaskIds = new ArrayList<>(allHierarchyElements.size());
//        fillDeletedProjectTasksRecursively(rootItem, projectTaskIds);
//
//        return projectTaskIds;

    }

    private void fillDeletedProjectTasksRecursively(TreeItem<ProjectTask> treeItem, List<ProjectTask> projectTaskIds) {

        for (TreeItem<ProjectTask> currentTreeItem: treeItem.getChildren()) {
            fillDeletedProjectTasksRecursively(currentTreeItem, projectTaskIds);
            projectTaskIds.add(currentTreeItem.getValue());
        }

    }

}
