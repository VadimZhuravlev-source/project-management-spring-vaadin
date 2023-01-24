package com.pmvaadin.projecttasks.links.repositories;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkRepository extends Repository<LinkImpl, Integer> {

    <I> List<Link> findAllByProjectTaskIdOrderBySortAsc(I id);

    List<Link> findAllById(Iterable<Integer> ids);

    List<Link> saveAll(Iterable<? extends Link> links);

    void deleteAllById(Iterable<?> ids);

    @Query(value = "SELECT MAX(sort) FROM LinkImpl WHERE projectTaskId = :projectTaskId")
    <I, L> L findMaxSortOnProjectTask(@Param("projectTaskId") I projectTaskId);

    @Query(value = "SELECT * FROM links WHERE projectTaskId = :projectTaskId AND NOT id IN(:ids) ORDER BY row_order ASC", nativeQuery = true)
    <I> List<Link> findAllByProjectTaskIdAndNotIdIn(@Param("projectTaskId") I projectTaskId, @Param("ids") Iterable<?> ids);

}
