package com.pmvaadin.projecttasks.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ComboBoxDataProvider {

    int sizeInBackEnd(String filter, PageRequest pageable);
    List<ProjectTask> getItems(String filter, PageRequest pageable);

}
