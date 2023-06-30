package com.pmvaadin;

import com.pmvaadin.projectstructure.termscalculation.LinkedProjectTaskDTO;
import com.pmvaadin.projecttasks.entity.LinkedProjectTask;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CommandLineRunnerForTests implements CommandLineRunner {

    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    public void setProjectTaskRepository(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    @Override
    public void run(String...args) throws Exception {
        //Optional<ProjectTask> projectTask = projectTaskRepository.findById(3);
        ArrayList<Integer> ids = new ArrayList<>(2);
        ids.add(2);
        ids.add(10000);
        List<LinkedProjectTaskDTO> linkedProjectTasks = projectTaskRepository.findAllByIdIn(ids, LinkedProjectTaskDTO.class);
        linkedProjectTasks = null;
    }

}
