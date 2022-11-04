package com.pmvaadin.projecttasks.links.repositories;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkRepository extends Repository<LinkImpl, Integer> {

    List<? extends Link> findAll();

    List<? extends Link> findAllByProjectTaskId(Integer id);

    List<? extends Link> saveAll(Iterable<Link> links);

    void deleteAllById(Iterable<?> ids);

    @Query(value = "SELECT MAX(sort) FROM LinkImpl WHERE projectTaskId = :projectTaskId")
    <I, L> L findMaxSortOnProjectTask(@Param("projectTaskId") I projectTaskId);

}
