package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectTaskRepository extends Repository<ProjectTaskImpl, Integer> {

    List<ProjectTask> findAll();

    List<ProjectTask> findAllById(Iterable ids);

    ProjectTask findById(Integer id);

    void deleteAllById(Iterable ids);

    ProjectTask save(ProjectTask projectTask);

    @Query(value = "SELECT MAX(level_order) FROM project_tasks WHERE parent_id = :parentId",
            nativeQuery = true)
    Integer findMaxOrderIdOnParentLevel(@Param("parentId") Integer parentId);

    @Query(value = "SELECT MAX(level_order) FROM project_tasks WHERE parent_id is NULL",
            nativeQuery = true)
    Integer findMaxOrderIdOnParentLevelWhereParentNull();

}
