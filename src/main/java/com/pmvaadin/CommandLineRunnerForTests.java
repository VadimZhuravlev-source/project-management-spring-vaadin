package com.pmvaadin;

import com.pmvaadin.project.tasks.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
//        ArrayList<Integer> ids = new ArrayList<>(2);
//        ids.add(1);
//        ids.add(2);
//        ids.add(3);
//
//        List<LinkedProjectTaskDTO> linkedProjectTasks = projectTaskRepository.findAllByIdIn(ids, LinkedProjectTaskDTO.class);
        //List<BaseProjectItem> list = linkedProjectTasks.stream().map(b -> (BaseProjectItem) b).toList();
        //        linkedProjectTasks = null;
    }

}
