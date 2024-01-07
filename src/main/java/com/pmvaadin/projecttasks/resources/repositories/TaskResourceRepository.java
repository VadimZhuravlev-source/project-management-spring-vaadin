package com.pmvaadin.projecttasks.resources.repositories;

import com.pmvaadin.projecttasks.resources.entity.TaskResource;
import com.pmvaadin.projecttasks.resources.entity.TaskResourceImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TaskResourceRepository extends Repository<TaskResourceImpl, Integer> {

    <I> List<TaskResource> findByProjectTaskIdOrderBySortAsc(I id);
    List<TaskResource> save(List<TaskResource> taskResources);

    void deleteAllById(Iterable<?> ids);

}
