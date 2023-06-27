package com.pmvaadin;

import com.pmvaadin.projectstructure.TreeProjectTasks;
import com.pmvaadin.projectstructure.TreeProjectTasksImpl;
import com.pmvaadin.projectstructure.termscalculation.TermsCalculation;
import com.pmvaadin.projectstructure.termscalculation.TermsCalculationImpl;
import com.pmvaadin.projecttasks.dependencies.ProjectTasksIdConversion;
import com.pmvaadin.projecttasks.dependencies.ProjectTasksIdConversionWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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

}
