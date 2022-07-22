package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProjectTaskRepository extends Repository<ProjectTaskImpl, Integer> {

    List<ProjectTask> findAllByOrderByLevelOrderAsc();

    List<ProjectTask> findAllById(Iterable<Integer> ids);

    Optional<ProjectTask> findById(Integer id);

    void deleteAllById(Iterable<Integer> ids);

    ProjectTask save(ProjectTask projectTask);

    Iterable<ProjectTask> saveAll(Iterable<ProjectTask> ids);

    @Query(value = "SELECT MAX(level_order) FROM project_tasks WHERE parent_id = :parentId", nativeQuery = true)
    Integer findMaxOrderIdOnParentLevel(@Param("parentId") Integer parentId);

    @Query(value = "SELECT MAX(level_order) FROM project_tasks WHERE parent_id IS NULL", nativeQuery = true)
    Integer findMaxOrderIdOnParentLevelWhereParentNull();

    List<ProjectTask> findByParentIdInOrderByLevelOrderAsc(Collection<Integer> ids);

    @Query(value = "SELECT * FROM project_tasks WHERE parent_id IS NULL\n" +
            "UNION\n" +
            "SELECT * FROM project_tasks WHERE parent_id in (:parentIds)\n" +
            "ORDER BY level_order ASC", nativeQuery = true)
    List<ProjectTaskImpl> findByParentIdInWithNullOrderByLevelOrderAsc(@Param("parentIds") Iterable<Integer> ids);

}
