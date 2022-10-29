package com.pmvaadin.projecttasks.repositories;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProjectTaskRepository extends Repository<ProjectTaskImpl, Integer> {

    List<ProjectTask> findAllByOrderByLevelOrderAsc();

    List<ProjectTask> findAllById(Iterable<?> ids);

    Optional<ProjectTask> findById(Integer id);

    void deleteAllById(Iterable<?> ids);

    ProjectTask save(ProjectTask projectTask);

    List<ProjectTask> saveAll(Iterable<ProjectTask> ids);

    @Query(value = "SELECT MAX(level_order) FROM project_tasks WHERE parent_id = :parentId", nativeQuery = true)
    <I, L> L findMaxOrderIdOnParentLevel(@Param("parentId") I parentId);

    @Query(value = "SELECT MAX(level_order) FROM project_tasks WHERE parent_id IS NULL", nativeQuery = true)
    <L> L findMaxOrderIdOnParentLevelWhereParentNull();

    List<ProjectTask> findByParentIdInOrderByLevelOrderAsc(Collection<?> ids);

    @Query(value = "SELECT * FROM project_tasks WHERE parent_id IS NULL\n" +
            "UNION\n" +
            "SELECT * FROM project_tasks WHERE parent_id in (:parentIds)\n" +
            "ORDER BY level_order ASC", nativeQuery = true)
    List<ProjectTaskImpl> findByParentIdInWithNullOrderByLevelOrderAsc(@Param("parentIds") Iterable<?> ids);

}
