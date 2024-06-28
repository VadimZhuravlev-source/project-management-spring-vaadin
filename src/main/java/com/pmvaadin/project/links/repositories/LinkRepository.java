package com.pmvaadin.project.links.repositories;

import com.pmvaadin.project.links.entities.Link;
import com.pmvaadin.project.links.entities.LinkImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LinkRepository extends Repository<LinkImpl, Integer> {

    List<Link> findAllByProjectTaskIdOrderBySortAsc(Integer id);

    List<Link> findAllById(Iterable<?> ids);

    List<Link> saveAll(Iterable<? extends Link> links);

    void deleteAllById(Iterable<?> ids);

    @Query(value = "SELECT MAX(sort) FROM LinkImpl WHERE projectTaskId = :projectTaskId")
    <I, L> L findMaxSortOnProjectTask(@Param("projectTaskId") I projectTaskId);

    @Query(value = "SELECT * FROM links WHERE project_task = :projectTaskId AND id NOT IN (:ids) ORDER BY row_order ASC", nativeQuery = true)
    <I> List<Link> findAllByProjectTaskIdAndIdNotInIds(@Param("projectTaskId") I projectTaskId, @Param("ids") Iterable<?> ids);

    List<Link> findDistinctByProjectTaskIdIn(Collection<Integer> projectTaskIds);

    @Query(value = "SELECT l FROM LinkImpl l WHERE projectTaskId = :projectTaskId OR linkedProjectTaskId = :projectTaskId ORDER BY sort ASC")
    <I> List<Link> findAllWithSuccessorsByProjectTaskId(@Param("projectTaskId") I projectTaskId);


}
