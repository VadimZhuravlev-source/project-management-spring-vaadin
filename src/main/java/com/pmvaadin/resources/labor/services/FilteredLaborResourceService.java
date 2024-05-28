package com.pmvaadin.resources.labor.services;

import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentationDTO;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilteredLaborResourceService extends LaborResourceServiceImpl {

    private EntityManager entityManager;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<LaborResourceRepresentation> getItems(String filter, Pageable pageable) {
        return getItemList(filter, pageable);
    }

    @Override
    public int sizeInBackEnd(String filter, Pageable pageable) {
        return getItemNumber(String filter, Pageable pageable);
    }

    private List<LaborResourceRepresentation> getItemList(String filter, Pageable pageable) {

    }

    private int getItemNumber(String filter, Pageable pageable) {

    }

}
