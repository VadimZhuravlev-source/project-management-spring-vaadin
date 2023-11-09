package com.pmvaadin;

import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.projectstructure.TreeProjectTasksImpl;
import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import com.pmvaadin.terms.calculation.TermsCalculation;
import com.pmvaadin.terms.calculation.TermsCalculationImpl;
import com.pmvaadin.projecttasks.dependencies.ProjectTasksIdConversion;
import com.pmvaadin.projecttasks.dependencies.ProjectTasksIdConversionWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AppConfiguration {

    @Bean
    public TreeProjectTasks treeProjectTasks() {
        return new TreeProjectTasksImpl();
    }

    @Bean
    public ProjectTasksIdConversion idConversion() {
        return new ProjectTasksIdConversionWrapper();
    }

    @Bean
    public TermsCalculation termsCalculation() {
        return new TermsCalculationImpl();
    }

    @Bean
    public Link getLink() {
        return new LinkImpl();
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

}
