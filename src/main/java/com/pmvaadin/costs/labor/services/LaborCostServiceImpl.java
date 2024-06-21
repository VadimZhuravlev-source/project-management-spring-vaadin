package com.pmvaadin.costs.labor.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.costs.labor.entities.*;
import com.pmvaadin.costs.labor.repositories.LaborCostRepository;
import com.pmvaadin.projectstructure.StandardError;
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
import java.util.*;

@Service
public class LaborCostServiceImpl implements LaborCostService, ListService<LaborCostRepresentation, LaborCost> {

    private LaborCostRepository laborCostRepository;
    private LaborResourceService laborResourceService;
    private final String queryTextAvailableTasks = getQueryTextForAvailableTasks();
    private final String queryTextIntervalReps = getQueryTextForIntervalReps();
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
    }

    // ListService
    @Override
    public LaborCost save(LaborCost laborCost) {
        var day = laborCost.getDay();
        var resource = laborCost.getLaborResourceId();
        if (day == null
                || day.equals(LocalDate.MIN)
                || day.equals(LocalDate.MAX))
            throw new StandardError("Uncorrected day.");
        if (resource == null)
            throw new StandardError("Uncorrected labor resource.");
        var persistedResourceOpt = laborCostRepository.findByDayAndLaborResourceId(day, resource);
        if (persistedResourceOpt.isPresent()
                && !persistedResourceOpt.get().getId().equals(laborCost.getId()))
            throw new StandardError("Labor cost with the same day and resource already exists.");

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
        fillRepresentations(laborCost);
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

        var laborCost = laborCostRepository.findById(calRep.getId()).orElse(new LaborCostImpl());
        if (laborCost instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) laborCost).nullIdentifyingFields();
        fillRepresentations(laborCost);

        return laborCost;

    }

    private void fillRepresentations(LaborCost laborCost) {
        var resourceId = laborCost.getLaborResourceId();
        if (resourceId != null) {
            var resourceRep = laborResourceService.getById(resourceId);
            laborCost.setLaborResourceRepresentation(resourceRep);
        }
        fillIntervalReps(laborCost);
    }

    private void fillIntervalReps(LaborCost laborCost) {
        var ids = laborCost.getIntervals().stream().map(WorkInterval::getTaskId).filter(Objects::nonNull).distinct().toList();
        if (laborCost.getIntervals().isEmpty())
            return;
        var query = entityManager.createQuery(queryTextIntervalReps);
        query.setParameter("ids", ids);
        var resultList = (List<Object[]>) query.getResultList();
        var map = new HashMap<Object, ProjectTaskRep>();
        for (var row: resultList) {
            var rep = new ProjectTaskRepImpl((Integer) row[0], row[1].toString());
            map.put(rep.getId(), rep);
        }
        laborCost.getIntervals().forEach(workInterval -> {
            var rep = map.get(workInterval.getTaskId());
            if (rep != null)
                workInterval.setTaskName(rep.getRep());
        });

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

    private String getQueryTextForIntervalReps() {
        return """
                SELECT p.id, p.name
                FROM ProjectTaskImpl p
                WHERE
                p.id IN(:ids)
                """;
    }

}
