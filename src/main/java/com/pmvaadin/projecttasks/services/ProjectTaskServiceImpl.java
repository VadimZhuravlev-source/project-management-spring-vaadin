package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projectstructure.*;
import com.pmvaadin.common.tree.TreeItem;
import com.pmvaadin.projecttasks.entity.HierarchyElement;
import com.pmvaadin.projecttasks.services.role.level.security.ProjectTaskFilter;
import com.pmvaadin.security.entities.User;
import com.pmvaadin.security.services.UserService;
import com.pmvaadin.terms.calculation.TermCalculationRespond;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService, ComboBoxDataProvider {

    private ProjectTaskRepository projectTaskRepository;
    private ProjectRecalculation projectRecalculation;
    private ChangeHierarchyTransactionalService changeHierarchyTransactionalService;
    private HierarchyService hierarchyService;
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Autowired
    public void setProjectRecalculation(ProjectRecalculation projectRecalculation) {
        this.projectRecalculation = projectRecalculation;
    }

    @Autowired
    public void setChangeHierarchyTransactionalService(ChangeHierarchyTransactionalService changeHierarchyTransactionalService) {
        this.changeHierarchyTransactionalService = changeHierarchyTransactionalService;
    }

    @Autowired
    public void setHierarchyService(HierarchyService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
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

        var taskSaver = new TaskSaver();
        return taskSaver.save(projectTask, validate, recalculateTerms);

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

    @Override
    public int sizeInBackEnd(String filter, PageRequest pageable) {
        var security = new ProjectTaskFilter(entityManager, userService, projectTaskRepository);
        return security.sizeInBackEnd(filter, pageable);
    }

    @Override
    public List<ProjectTask> getItems(String filter, PageRequest pageable) {
        var security = new ProjectTaskFilter(entityManager, userService, projectTaskRepository);
        return security.getItems(filter, pageable);
    }

    @Override
    public Map<?, ProjectTask> getTasksById(Iterable<?> ids) {
//        var queryText = """
//                SELECT
//                	id,
//                	name
//                FROM
//                	project_tasks
//                WHERE
//                	id = ANY(:ids)
//                """;
//        var idsParameter = String.valueOf(ids).replace("[", "'{").replace("]", "}'");
//        queryText = queryText.replace(":ids", idsParameter);
//        var query = entityManager.createNativeQuery(queryText);
//        List<Object[]> resultList = query.getResultList();

        var foundTasks = projectTaskRepository.findAllById(ids);
        var map = new HashMap<Object, ProjectTask>();
        foundTasks.forEach(projectTask -> map.put(projectTask.getId(), projectTask));
//        for (var row: resultList) {
//            var name = row[1];
//            if (name == null)
//                name = "";
//            map.put(row[0], name.toString());
//        }

        return map;
    }



    private void populateListByRootItemRecursively(List<ProjectTask> projectTasks, TreeItem<ProjectTask> treeItem) {

        for (TreeItem<ProjectTask> child : treeItem.getChildren()) {
            projectTasks.add(child.getValue());
            populateListByRootItemRecursively(projectTasks, child);
        }

    }

    private class TaskSaver {

        private ProjectTask projectTask;
        private boolean isNew;
        private User user;
        private boolean isUserFollowingRLS;
        private Object parentId;
        private boolean addProjectToUserProjects;

        public ProjectTask save(ProjectTask projectTask, boolean validate, boolean recalculateTerms) {

            this.projectTask = projectTask;
            this.isNew = projectTask.isNew();

            if (validate && !validate(projectTask))
                return projectTask;

            fillNecessaryFieldsIfItIsNew(projectTask);

            var savedProjectTask = projectTaskRepository.save(projectTask);

            if (isUserFollowingRLS && addProjectToUserProjects) {
                var list = new ArrayList<ProjectTask>(1);
                list.add(savedProjectTask);
                userService.addProjectTaskToUserProject(list, this.user);
            }

            if (!recalculateTerms) {
                return savedProjectTask;
            }
            changeHierarchyTransactionalService.recalculateTerms(savedProjectTask);
            return savedProjectTask;

        }

        private void fillNecessaryFieldsIfItIsNew(ProjectTask projectTask) {

            if (!isNew) return;

            user = userService.getCurrentUser();
            isUserFollowingRLS = userService.isUserFollowingRLS(user);

            parentId = projectTask.getParentId();
            setCorrespondParentId();

            setLevelOrder();

            // set Terms
            if (projectTask.getDuration() == 0)
                projectTask.setDuration(Calendar.DAY_DURATION_SECONDS);

            if (projectTask.getStartDate() == null || projectTask.getFinishDate() == null) {
                var now = LocalDateTime.now();
                // TODO recalculation of the dates by a task's calendar
                //  or to compare with ProjectData's filling of this necessitated fields
                projectTask.setStartDate(now);
                projectTask.setFinishDate(now.plusDays(1));
            }

        }

        private void setCorrespondParentId() {

            if (parentId != null) {
                // Validate parent existence
                var foundParentOpt = projectTaskRepository.findById(parentId);
                if (foundParentOpt.isEmpty()) {
                    projectTask.setParentId(null);
                    parentId = null;
                }
            }

            if (!isUserFollowingRLS)
                return;

            if (parentId == null) {
                setParentAsUserRoot();
                return;
            }

            var list = new ArrayList<>();
            list.add(parentId);
            var parents = hierarchyService.getParentsOfParent(list);
            var task = parents.stream().filter(p -> parentId.equals(p.getParentId())).findFirst().orElse(null);
            if (task == null) {
                setParentAsUserRoot();
                return;
            }
            List<HierarchyElement<Object>> elements = new ArrayList<>(1);
            elements.add((HierarchyElement) task);
            var accessTable = userService.getUserAccessTable(elements, user, parents);
            var accessRights = accessTable.get(parentId);
            if (accessRights.isCurrentTaskAnAllowedProject()
                    || accessRights.isOneOfAllowedChildren()) {
                return;
            }
            setParentAsUserRoot();
        }

        private void setParentAsUserRoot() {
            var rootProjectId = user.getRootProjectId();
            projectTask.setParentId(rootProjectId);
            parentId = rootProjectId;
            addProjectToUserProjects = true;
        }

        private void setLevelOrder() {

            Integer levelOrder;
            if (parentId == null) {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevelWhereParentNull();
            } else {
                levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parentId);
            }
            if (levelOrder == null) levelOrder = 1;
            projectTask.setLevelOrder(levelOrder);

        }

    }

}
