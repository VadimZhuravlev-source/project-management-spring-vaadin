package com.pmvaadin.costs.labor.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.LaborCostImpl;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentation;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentationDTO;
import com.pmvaadin.costs.labor.repositories.LaborCostRepository;
import com.pmvaadin.projecttasks.entity.ProjectTaskRep;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class LaborCostServiceImpl implements LaborCostService, ListService<LaborCostRepresentation, LaborCost> {

    private LaborCostRepository laborCostRepository;

    @Autowired
    public void setLaborCostRepository(LaborCostRepository laborCostRepository) {
        this.laborCostRepository = laborCostRepository;
    }

    @Override
    public List<ProjectTaskRep> getAvailableTasks(LaborResourceRepresentation laborResource, LocalDate day) {

    }

    // ListService
    @Override
    public LaborCost save(LaborCost laborCost) {
        return laborCostRepository.save(laborCost);
    }

    @Override
    public List<LaborCostRepresentation> getItems(String filter, Pageable pageable) {
        var items = laborCostRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, LaborCostRepresentationDTO.class);
        return items.stream().map(l -> (LaborCostRepresentation) l).toList();
    }

    @Override
    public int sizeInBackEnd(String filter, Pageable pageable) {
        return laborCostRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, LaborCostRepresentationDTO.class).size();
    }

    @Override
    public LaborCost add() {

        return new LaborCostImpl();

    }

    @Override
    public LaborCost get(LaborCostRepresentation representation) {
        return laborCostRepository.findById(representation.getId()).get();
    }

    @Transactional
    @Override
    public boolean delete(Collection<LaborCostRepresentation> reps) {

        var ids = reps.stream().map(LaborCostRepresentation::getId).filter(Objects::nonNull).distinct().toList();
        laborCostRepository.deleteAllById(ids);

        return true;

    }

    @Override
    public LaborCost copy(LaborCostRepresentation calRep) {

        LaborCost laborCost = laborCostRepository.findById(calRep.getId()).orElse(new LaborCostImpl());
        if (laborCost instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) laborCost).nullIdentifyingFields();

        return laborCost;

    }

}
