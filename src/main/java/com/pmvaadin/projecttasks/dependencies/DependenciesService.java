package com.pmvaadin.projecttasks.dependencies;

import com.pmvaadin.terms.calculation.TermCalculationData;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

public interface DependenciesService {

    <I> DependenciesSet getAllDependencies(I parentId, List<I> ids);

    <I> DependenciesSet getAllDependenciesWithCheckedChildren(I parentId, List<I> ids);

    String getCycleLinkMessage(DependenciesSet dependenciesSet);

    <I> TermCalculationData getAllDependenciesForTermCalc(EntityManager entityManager, Set<I> ids);

}
