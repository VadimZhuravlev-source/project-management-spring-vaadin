package com.pmvaadin.project.tasks.services;

import com.pmvaadin.project.tasks.entity.ProjectTask;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ComboBoxDataProvider {

    int sizeInBackEnd(String filter, PageRequest pageable);

    List<ProjectTask> getItems(String filter, PageRequest pageable);

}
