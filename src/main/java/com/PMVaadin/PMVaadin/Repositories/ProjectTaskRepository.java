package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;
import com.PMVaadin.PMVaadin.Entities.ProjectTaskImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ProjectTaskRepository extends Repository<ProjectTaskImpl, Integer> {

    List<ProjectTask> findAll();

    List<ProjectTask> findAllById(Iterable ids);

    ProjectTask findById(Integer id);

    void deleteAllById(Iterable ids);

}
