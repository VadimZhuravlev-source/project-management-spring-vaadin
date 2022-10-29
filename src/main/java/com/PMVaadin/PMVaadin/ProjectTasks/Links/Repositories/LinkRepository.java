package com.PMVaadin.PMVaadin.ProjectTasks.Links.Repositories;

import com.PMVaadin.PMVaadin.ProjectTasks.Links.Entities.Link;
import com.PMVaadin.PMVaadin.ProjectTasks.Links.Entities.LinkImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface LinkRepository extends Repository<LinkImpl, Integer> {

    List<Link> findAll();

    List<Link> findAllByProjectTaskId(Integer id);

}
