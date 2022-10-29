package com.pmvaadin.projecttasks.links.repositories;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface LinkRepository extends Repository<LinkImpl, Integer> {

    List<Link> findAll();

    List<Link> findAllByProjectTaskId(Integer id);

}
