package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("prototype")
public class TermsCalculationImpl implements TermsCalculation {

    @Override
    public Set<ProjectTask> calculate(DependenciesSet dependenciesSet) {

        if (dependenciesSet.isCycle()) throw new StandardError("Cycle detected in the dependent tasks. Terms calculation is not possible.");

        return new HashSet<>(0);

    }



}
