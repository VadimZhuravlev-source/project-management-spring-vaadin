package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projectstructure.*;
import com.pmvaadin.common.tree.TreeItem;
import com.pmvaadin.terms.calculation.TermCalculationRespond;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private ProjectTaskRepository projectTaskRepository;
    private ProjectRecalculation projectRecalculation;
    private ChangeHierarchyTransactionalService changeHierarchyTransactionalService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository){
        this.projectTaskRepository = projectTaskRepository;
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
        var treeProjectTasks = new TreeProjectTasksImpl();
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
        changeHierarchyTransactionalService.recalculateTerms(savedProjectTask);
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
    public void delete(List<? extends ProjectTask> projectTasks) {

        var respond = changeHierarchyTransactionalService.delete(projectTasks);
        projectRecalculation.recalculate(respond.getRecalculatedProjects());

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
    public void changeLocation(Set<ProjectTask> projectTasks, ProjectTask target, GridDropLocation dropLocation) {

        var respond = changeHierarchyTransactionalService.moveTasksInHierarchy(projectTasks, target, dropLocation);
        this.projectRecalculation.recalculate(respond.getRecalculatedProjects());

    }

    @Override
    public void changeLocation(Set<ProjectTask> projectTasks, Direction direction) {

        var respond = changeHierarchyTransactionalService.moveTasksInHierarchy(projectTasks, direction);
        this.projectRecalculation.recalculate(respond.getRecalculatedProjects());

    }

    @Override
    public void changeSortOrder(Set<ProjectTask> projectTasks, Direction direction) {

        changeHierarchyTransactionalService.changeSortOrder(projectTasks, direction);

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

    private void populateListByRootItemRecursively(List<ProjectTask> projectTasks, TreeItem<ProjectTask> treeItem) {

        for (TreeItem<ProjectTask> child: treeItem.getChildren()) {
            projectTasks.add(child.getValue());
            populateListByRootItemRecursively(projectTasks, child);
        }

    }

}
