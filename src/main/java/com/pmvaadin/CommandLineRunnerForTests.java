package com.pmvaadin;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.projecttasks.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
    }

}
