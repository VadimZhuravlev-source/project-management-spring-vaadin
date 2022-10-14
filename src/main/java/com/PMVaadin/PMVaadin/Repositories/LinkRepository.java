package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.Links.Link;
import com.PMVaadin.PMVaadin.Entities.Links.LinkImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface LinkRepository extends Repository<LinkImpl, Integer> {

    List<Link> findAll();

    List<Link> findAllByProjectTaskId(Integer id);

}
