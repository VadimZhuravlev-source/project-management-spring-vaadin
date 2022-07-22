package com.PMVaadin.PMVaadin;

import com.PMVaadin.PMVaadin.ProjectStructure.TreeProjectTasks;
import com.PMVaadin.PMVaadin.ProjectStructure.TreeProjectTasksImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public TreeProjectTasks treeProjectTasks() {
        return new TreeProjectTasksImpl();
    }


}
