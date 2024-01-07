package com.pmvaadin.projecttasks.resources.services;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.data.ProjectTaskData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.resources.entity.TaskResource;
import com.pmvaadin.projecttasks.resources.entity.TaskResourceImpl;
import com.pmvaadin.projecttasks.resources.repositories.TaskResourceRepository;
import com.pmvaadin.resources.entity.LaborResourceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskResourceServiceImpl implements TaskResourceService {

    @Autowired
    private TaskResourceRepository taskResourceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TaskResource> getLaborResources(ProjectTask projectTask) {

        var queryText = getProjectTaskLaborResourcesQueryText();
        var query = entityManager.createNativeQuery(queryText);//, Map.class);
        query.setParameter("id", projectTask.getId());
        var result = (List<Map<String, Object>>) query.getResultList();

        return result.stream().map(this::getTaskResourceByMap).collect(Collectors.toList());

    }

    @Override
    public List<TaskResource> save(ProjectTaskData projectTaskData) {

        fillSort(projectTaskData);
        var deletedRows = determineChanges(projectTaskData);
        if (!deletedRows.isEmpty()) {
            var ids = deletedRows.stream().map(TaskResource::getId).toList();
            taskResourceRepository.deleteAllById(ids);
        }
        var resources = projectTaskData.getTaskResources();
        return taskResourceRepository.save(resources);

    }

    @Override
    public boolean validate(List<TaskResource> resources) {
        resources.forEach(resource -> {
            if (resource.getResourceId() == null)
                throw new StandardError("The resource can not be null.");
            if (resource.getProjectTaskId() == null)
                throw new StandardError("The project task id can not be null.");
            if (resource.getDuration() == null || resource.getDuration().compareTo(new BigDecimal(0)) < 0)
                throw new StandardError("The duration must be greater than 0.");
        });
        return true;
    }

    private void fillSort(ProjectTaskData projectTaskData) {
        var resources = projectTaskData.getTaskResources();
        var sort = 0;
        for (var resource: resources) {
            resource.setSort(sort++);
        }
    }

    private List<TaskResource> determineChanges(ProjectTaskData projectTaskData) {
        var resources = projectTaskData.getTaskResources();

        ProjectTask projectTask = projectTaskData.getProjectTask();
        var resourcesInBase = taskResourceRepository.findByProjectTaskIdOrderBySortAsc(projectTask.getId());

        var isResourcesChanged = isResourcesChanged(resources, resourcesInBase);
        if (isResourcesChanged) {
            var checkSum = projectTask.getResourcesCheckSum();
            projectTask.setResourcesCheckSum(checkSum++);
        }

        var ids = resources.stream().map(TaskResource::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        var deletedRows = new ArrayList<TaskResource>();
        for (var resource: resourcesInBase) {
            if (!ids.contains(resource.getId())) deletedRows.add(resource);
        }

        return deletedRows;

    }

    private boolean isResourcesChanged(List<TaskResource> current, List<TaskResource> inBase) {

        if (current.size() != inBase.size()) {
            return true;
        }

        var index = 0;
        for(var resource: current) {
            var inBaseResource = inBase.get(index);
            var isEquals = isEquals(inBaseResource, resource);
            if (!isEquals)
                return true;
            index++;
        }

        return false;

    }

    private boolean isEquals(TaskResource resource1, TaskResource resource2) {
        return Objects.equals(resource1.getId(), resource2.getId())
                && Objects.equals(resource1.getProjectTaskId(), resource2.getProjectTaskId())
                && Objects.equals(resource1.getResourceId(), resource2.getResourceId())
                && Objects.equals(resource1.getDuration(), resource2.getDuration())
                && Objects.equals(resource1.getSort(), resource2.getSort());
    }

    private TaskResource getTaskResourceByMap(Map<String, Object> mapRow) {

        var id = (Integer) mapRow.get("id");
        var version = (Integer) mapRow.get("version");
        var projectTaskId = (Integer) mapRow.get("project_task_id");
        var resourceId = (Integer) mapRow.get("resource_id");
        var duration = (BigDecimal) mapRow.get("duration");
        var sort = (int) mapRow.get("sort");
        var name = (String) mapRow.get("name");
        var laborResource = new LaborResourceImpl();
        laborResource.setName(name);
        laborResource.setId(resourceId);
        return new TaskResourceImpl(id, version, projectTaskId,
                resourceId, duration, sort, laborResource);

    }

    private String getProjectTaskLaborResourcesQueryText() {
        return """
                SELECT
                    task_labor_resources.*,
                    labor_resources.name
                    
                FROM task_labor_resources
                    JOIN labor_resources
                        ON task_labor_resources.resource_id = labor_resources.id
                WHERE
                    project_task_id = :id
                """;
    }

}
