package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.projecttasks.dependencies.DependenciesSet;
import com.pmvaadin.projecttasks.entity.ProjectTask;

import java.util.Set;

public interface TermsCalculation {

    TermCalculationRespond calculate(TermCalculationData termCalculationData);

}
