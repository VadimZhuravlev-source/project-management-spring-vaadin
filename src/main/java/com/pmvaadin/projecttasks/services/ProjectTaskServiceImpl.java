package com.pmvaadin.projecttasks.services;

import com.pmvaadin.commonobjects.tree.TreeItem;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projectstructure.TestCase;
import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.projecttasks.dependencies.DependenciesService;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import com.pmvaadin.projecttasks.entity.ProjectTaskOrderedHierarchy;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        fillNecessaryFieldsIfIsNew(projectTask);
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
    @Transactional
    public void changeParent(Set<ProjectTask> projectTasks, ProjectTask parent) {

        if (projectTasks.contains(parent))
            throw new IllegalArgumentException("The parent has not to be contained in changed project tasks");

        List<ProjectTask> projectTaskList = new ArrayList<>(projectTasks);
        projectTaskList.add(parent);

        List<?> ids = projectTaskList.stream().map(ProjectTask::getId).collect(Collectors.toList());

        List<ProjectTask> projectTasksInBase = projectTaskRepository.findAllById(ids);
        Map<ProjectTask, ProjectTask> foundProjectTasks =
                projectTasksInBase.stream().collect(Collectors.toMap(p -> p, p -> p));

        validateVersion(projectTaskList, foundProjectTasks);

        Map<ProjectTask, ProjectTask> parentsOfParentMap = hierarchyService.getParentsOfParent(parent).stream().collect(
                Collectors.toMap(p -> p, p -> p));

        var parentIds = new ArrayList<>();
        Integer levelOrder = projectTaskRepository.findMaxOrderIdOnParentLevel(parent.getId());
        if (levelOrder == null) levelOrder = 0;
        projectTaskList = new ArrayList<>(projectTasks);
        for (ProjectTask projectTask: projectTaskList) {
            if (parentsOfParentMap.get(projectTask) != null)
                // Detected circle in tree
                throw new StandardError("Project structure has been changed. Should update the project and try again.");
            ProjectTask projectTaskInBase = foundProjectTasks.get(projectTask);
            if (!projectTask.getVersion().equals(projectTaskInBase.getVersion()))
                throw new StandardError("The task " + projectTask + " has been changed by an another user. Should update the project and try again.");
            parentIds.add(projectTask.getParentId());
            projectTask.setParentId(parent.getId());
            projectTask.setLevelOrder(++levelOrder);
        }

        var checkedIds = projectTasks.stream().map(ProjectTask::getId).toList();
        DependenciesSet dependenciesSet = dependenciesService.getAllDependenciesWithCheckedChildren(parent.getId(), checkedIds);

        if (dependenciesSet.isCycle()) {
            dependenciesSet.fillWbs(this);
            String message = dependenciesService.getCycleLinkMessage(dependenciesSet);
            throw new StandardError(message);
        }

        recalculateTerms(dependenciesSet);

        projectTaskRepository.saveAll(projectTaskList);
        List<ProjectTask> savedElements = recalculateLevelOrderForChildrenOfProjectTaskIds(parentIds);
        projectTaskRepository.saveAll(savedElements);

    }

    @Override
    public List<ProjectTask> swap(Map<ProjectTask, ProjectTask> swappedTasks) {

        ProjectTask projectTask1 = null, projectTask2 = null;
        for (Map.Entry<ProjectTask, ProjectTask> kv: swappedTasks.entrySet()) {
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


        //TODO do through changeParent




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

    private void fillNecessaryFieldsIfIsNew(ProjectTask projectTask) {

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

    private void validateVersion(Collection<ProjectTask> projectTasks, Map<ProjectTask, ProjectTask> projectTasksInBase) {

        if (projectTasks.size() != projectTasksInBase.size())
            throw new StandardError("Incorrect data. Should update the project and try again.");

        projectTasks.forEach(projectTask -> {
            ProjectTask projectTaskInBase = projectTasksInBase.getOrDefault(projectTask, null);
            if (projectTaskInBase == null || !projectTask.getVersion().equals(projectTaskInBase.getVersion()))
                throw new StandardError("The task " + projectTask + " has been changed by an another user. Should update the project and try again.");
        });

    }

    private void recalculateTerms(DependenciesSet dependenciesSet) {
        // TODO recalculate terms
    }

    private void recalculateTerms(ProjectTask projectTask) {
        // TODO recalculate terms
        // TODO getting dependencies and check changing terms
        //recalculateTerms(DependenciesSet dependenciesSet);
    }

    private void populateListByRootItemRecursively(List<ProjectTask> projectTasks, TreeItem<ProjectTask> treeItem) {

        for (TreeItem<ProjectTask> child: treeItem.getChildren()) {
            projectTasks.add(child.getValue());
            populateListByRootItemRecursively(projectTasks, child);
        }

    }

    private List<ProjectTask> recalculateLevelOrderForChildrenOfProjectTaskIds(List<?> parentIds) {

        List<ProjectTask> foundProjectTasks;

        if (parentIds.stream().anyMatch(Objects::isNull)) {
            List<?> findingParentIds = parentIds.stream().filter(Objects::nonNull).collect(Collectors.toList());
            List<ProjectTaskImpl> foundProjectTasks1 =
                    projectTaskRepository.findByParentIdInWithNullOrderByLevelOrderAsc(findingParentIds);
            foundProjectTasks = foundProjectTasks1.stream()
                    .map(projectTaskImpl -> (ProjectTask) projectTaskImpl)
                    .toList();
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
