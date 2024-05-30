package com.pmvaadin.resources.labor.services;

import com.pmvaadin.resources.labor.entity.LaborResource;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;

import java.util.Collection;
import java.util.List;

public interface LaborResourceService {

    LaborResource save(LaborResource laborResource);
    <I> LaborResourceRepresentation getById(I id);
    <I> List<LaborResourceRepresentation> getAllById(Collection<I> ids);

}
