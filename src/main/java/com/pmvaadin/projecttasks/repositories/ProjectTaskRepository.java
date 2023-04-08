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

    List<ProjectTask> saveAll(Iterable<ProjectTask> projectTasks);

    @Query(value = "SELECT MAX(level_order) FROM project_tasks WHERE parent_id = :parentId", nativeQuery = true)
    <I, L> L findMaxOrderIdOnParentLevel(@Param("parentId") I parentId);

    @Query(value = "SELECT MAX(level_order) FROM project_tasks WHERE parent_id IS NULL", nativeQuery = true)
    <L> L findMaxOrderIdOnParentLevelWhereParentNull();

    List<ProjectTask> findByParentIdInOrderByLevelOrderAsc(Collection<?> ids);
    List<ProjectTask> findByParentIdOrderByLevelOrderAsc(Integer id);

    List<ProjectTask> findByParentIdIsNullOrderByLevelOrderAsc();

    @Query(value = """            
            SELECT * FROM project_tasks WHERE parent_id IS NULL
            UNION
            SELECT * FROM project_tasks WHERE parent_id in (:parentIds)
            ORDER BY level_order ASC
            """, nativeQuery = true)
    List<ProjectTaskImpl> findByParentIdInWithNullOrderByLevelOrderAsc(@Param("parentIds") Iterable<?> ids);

    @Query(value = "SELECT COUNT(id) FROM ProjectTaskImpl WHERE parent_id = :parentId")
    int getChildrenCount(@Param("parentId") Integer parentId);

    @Query(value = "SELECT COUNT(id) FROM ProjectTaskImpl WHERE parent_id IS NULL")
    int getChildrenCount();

    @Query(value = """
            WITH found_task AS (
            SELECT parent_id, level_order FROM project_tasks WHERE id = :id
            )
            SELECT *
            FROM project_tasks
            	JOIN found_task
            	ON project_tasks.parent_id = found_task.parent_id
            WHERE
            	project_tasks.id NOT IN(:excludedIds)
            	AND project_tasks.level_order > found_task.level_order
            ORDER BY
            	project_tasks.level_order
            """, nativeQuery = true)
    <I> List<ProjectTask> findTasksThatFollowTargetWithoutExcludedTasks(@Param("id") I targetId, @Param("excludedIds") Iterable<?> excludedIds);

}
