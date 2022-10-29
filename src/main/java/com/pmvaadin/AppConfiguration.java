package com.pmvaadin;

import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.projectstructure.TreeProjectTasksImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public TreeProjectTasks treeProjectTasks() {
        return new TreeProjectTasksImpl();
    }


}
