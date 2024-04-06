package com.pmvaadin.projecttasks.repositories;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTaskImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ProjectTaskRepository extends Repository<ProjectTaskImpl, Integer> {

    List<ProjectTask> findAllByOrderByLevelOrderAsc();

    List<ProjectTask> findAllById(Iterable<?> ids);

    <T> List<T> findAllByIdIn(@Param("id") Iterable<?> ids, Class<T> type);

    <I> Optional<ProjectTask> findById(I id);

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
    List<ProjectTask> findByParentIdAndIdNotInOrderByLevelOrderAsc(Integer id, Iterable<?> excludedIds);

    List<ProjectTask> findByParentIdIsNullAndIdNotInOrderByLevelOrderAsc(Iterable<?> excludedIds);

    @Query(value = """            
            SELECT * FROM project_tasks WHERE parent_id IS NULL
            UNION
            SELECT * FROM project_tasks WHERE parent_id in (:parentIds)
            ORDER BY level_order ASC
            """, nativeQuery = true)
    List<ProjectTaskImpl> findByParentIdInWithNullOrderByLevelOrderAscInner(@Param("parentIds") Iterable<?> ids);

    default List<ProjectTask> findByParentIdInWithNullOrderByLevelOrderAsc(Iterable<?> ids) {
        List<ProjectTaskImpl> foundProjectTasks = findByParentIdInWithNullOrderByLevelOrderAscInner(ids);
        return foundProjectTasks.stream().map(projectTask -> (ProjectTask) projectTask).collect(Collectors.toList());

    }

    @Query(value = "SELECT COUNT(id) FROM ProjectTaskImpl WHERE parentId = :parentId")
    int getChildrenCount(@Param("parentId") Integer parentId);

    @Query(value = "SELECT COUNT(id) FROM ProjectTaskImpl WHERE parentId IS NULL")
    int getChildrenCount();

    @Query(value = "SELECT COUNT(id) FROM ProjectTaskImpl WHERE parentId = :parentId AND id NOT IN(:excludedIds)")
    int getChildrenCountWithExcludedTasks(@Param("parentId") Integer parentId, @Param("excludedIds") Iterable<?> excludedIds);

    @Query(value = "SELECT COUNT(id) FROM ProjectTaskImpl WHERE parentId IS NULL AND id NOT IN(:excludedIds)")
    int getChildrenCountWithExcludedTasks(@Param("excludedIds") Iterable<?> excludedIds);

    @Query(value = """
            WITH found_task AS (
            SELECT parent_id, level_order FROM project_tasks WHERE id = :id
            )
            SELECT project_tasks.*
            FROM project_tasks
            	JOIN found_task
                    ON
                        CASE WHEN found_task.parent_id IS NULL
                            THEN project_tasks.parent_id IS NULL
                            ELSE project_tasks.parent_id = found_task.parent_id
                        END
            WHERE
            	project_tasks.id NOT IN(:excludedIds)
            	AND project_tasks.level_order > found_task.level_order
            ORDER BY
            	project_tasks.level_order
            """, nativeQuery = true)
    <I> List<ProjectTaskImpl> findTasksThatFollowAfterTargetWithoutExcludedTasksInner(@Param("id") I targetId, @Param("excludedIds") Iterable<?> excludedIds);

    default <I> List<ProjectTask> findTasksThatFollowAfterTargetWithoutExcludedTasks(I targetId, Iterable<?> excludedIds) {
        List<ProjectTaskImpl> foundProjectTasks = findTasksThatFollowAfterTargetWithoutExcludedTasksInner(targetId, excludedIds);
        return foundProjectTasks.stream().map(projectTask -> (ProjectTask) projectTask).collect(Collectors.toList());
    }

    @Query(value = """
            WITH found_task AS (
            SELECT id, parent_id, level_order FROM project_tasks WHERE id IN(:ids)
            )
            SELECT project_tasks.*
            FROM project_tasks
                JOIN found_task
                    ON
                        CASE WHEN found_task.parent_id IS NULL
                            THEN project_tasks.parent_id IS NULL
                            ELSE project_tasks.parent_id = found_task.parent_id
                        END
            WHERE
                project_tasks.id NOT IN(:ids)
                AND project_tasks.level_order = found_task.level_order + :direction
            """, nativeQuery = true)
    List<ProjectTaskImpl> findTasksThatFollowBeforeGivenTasksIds(@Param("ids") Iterable<?> tasksIds, @Param("direction") int direction);

    default List<ProjectTask> findTasksThatFollowToGivenDirection(Iterable<?> tasksIds, int direction) {
        List<ProjectTaskImpl> foundProjectTasks = findTasksThatFollowBeforeGivenTasksIds(tasksIds, direction);
        return foundProjectTasks.stream().map(projectTask -> (ProjectTask) projectTask).collect(Collectors.toList());
    }

    @Query(value = """
            WITH found_task AS (
            SELECT id, parent_id, level_order FROM project_tasks WHERE id IN(:ids)
            )
            SELECT DISTINCT
                project_tasks.*
            FROM project_tasks
                JOIN found_task
                    ON
                        CASE WHEN found_task.parent_id IS NULL
                            THEN project_tasks.parent_id IS NULL
                            ELSE project_tasks.parent_id = found_task.parent_id
                        END
            WHERE
                project_tasks.level_order >= found_task.level_order
            ORDER BY
                project_tasks.parent_id,
                project_tasks.level_order
            """, nativeQuery = true)
    List<ProjectTaskImpl> findTasksThatFollowAfterGivenTasksIds(@Param("ids") Iterable<?> tasksIds);

    default List<ProjectTask> findTasksThatFollowAfterGivenTasks(Iterable<?> tasksIds) {
        List<ProjectTaskImpl> foundProjectTasks = findTasksThatFollowAfterGivenTasksIds(tasksIds);
        return foundProjectTasks.stream().map(projectTask -> (ProjectTask) projectTask).collect(Collectors.toList());
    }

    List<ProjectTask> findByNameLikeIgnoreCase(String name, Pageable pageable);
//    int countByNameIgnoreCase(String name, Pageable pageable);

}
