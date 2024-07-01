package com.pmvaadin.project.resources.labor.repositories;

import com.pmvaadin.project.resources.labor.entity.TaskResource;
import com.pmvaadin.project.resources.labor.entity.TaskResourceImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TaskResourceRepository extends Repository<TaskResourceImpl, Integer> {

    List<TaskResource> findByProjectTaskIdOrderBySortAsc(Integer id);
    TaskResource save(TaskResource taskResources);
    List<TaskResource> saveAll(Iterable<? extends TaskResource> taskResources);

    void deleteAllById(Iterable<?> ids);

}
