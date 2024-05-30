package com.pmvaadin.costs.labor.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.LaborCostImpl;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentation;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentationDTO;
import com.pmvaadin.costs.labor.repositories.LaborCostRepository;
import com.pmvaadin.projecttasks.entity.ProjectTaskRep;
import com.pmvaadin.projecttasks.entity.ProjectTaskRepImpl;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.services.LaborResourceService;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class LaborCostServiceImpl implements LaborCostService, ListService<LaborCostRepresentation, LaborCost> {

    private LaborCostRepository laborCostRepository;
    private LaborResourceService laborResourceService;
    private final String queryTextAvailableTasks = getQueryTextForAvailableTasks();
    private EntityManager entityManager;

    @Autowired
    public void setLaborCostRepository(LaborCostRepository laborCostRepository) {
        this.laborCostRepository = laborCostRepository;
    }

    @Autowired
    @Qualifier("LaborResourceService")
    public void setLaborResourceService(LaborResourceService laborResourceService) {
        this.laborResourceService = laborResourceService;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<ProjectTaskRep> getAvailableTasks(LaborResourceRepresentation laborResource, LocalDate day) {
        var query = entityManager.createQuery(queryTextAvailableTasks);
        query.setParameter("day", day);
        query.setParameter("resourceId", laborResource.getId());
        var resultList = (List<Object[]>) query.getResultList();
        var result = new ArrayList<ProjectTaskRep>();
        for (var row: resultList) {
            var rep = new ProjectTaskRepImpl((Integer) row[0], row[1].toString());
            result.add(rep);
        }
        return result;
//        return query.getResultStream().map(p -> (ProjectTaskRep) p).toList();
//        var availableTasks = laborCostRepository.getAvailableTasks(laborResource.getId(), day, ProjectTaskRepImpl.class);
//        return new ArrayList<>(availableTasks);
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
        var laborCost = laborCostRepository.findById(representation.getId()).orElse(new LaborCostImpl());
        var resourceRep = laborResourceService.getById(laborCost.getLaborResourceId());
        laborCost.setLaborResourceRepresentation(resourceRep);
        return laborCost;
    }

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

    private String getQueryTextForAvailableTasks() {
        return """
                SELECT p.id, p.name
                FROM ProjectTaskImpl p
                WHERE
                CAST(p.startDate AS DATE) <= :day
                AND CAST(p.finishDate AS DATE) >= :day
                AND id IN(
                SELECT tr.projectTaskId
                FROM TaskResourceImpl tr
                WHERE tr.resourceId = :resourceId)
                """;
    }

}
