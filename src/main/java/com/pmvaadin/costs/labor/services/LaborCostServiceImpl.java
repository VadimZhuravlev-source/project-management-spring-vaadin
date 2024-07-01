package com.pmvaadin.costs.labor.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.costs.labor.entities.*;
import com.pmvaadin.costs.labor.repositories.LaborCostRepository;
import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.project.tasks.entity.ProjectTaskRep;
import com.pmvaadin.project.tasks.entity.ProjectTaskRepImpl;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.services.LaborResourceService;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class LaborCostServiceImpl implements LaborCostService, ListService<LaborCostRepresentation, LaborCost> {

    private LaborCostRepository laborCostRepository;
    private LaborResourceService laborResourceService;
    private final String queryTextAvailableTasks = getQueryTextForAvailableTasks();
    private final String queryTextIntervalReps = getQueryTextForIntervalReps();
    private final String queryTextLaborCosts = getQueryTextForLaborCosts();
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
        var result = new ArrayList<ProjectTaskRep>(resultList.size());
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

        var savedItem = laborCostRepository.save(laborCost);
        fillRepresentations(savedItem);
        return savedItem;

    }

    @Override
    public List<LaborCostRepresentation> getItems(String filter, Pageable pageable) {

        var userDetails = getUserDetails();
        if (userDetails == null || userDetails.getAuthorities().isEmpty())
            return new ArrayList<>(0);
        return getItemsCountingUserRights(filter, userDetails.getUsername(), pageable);
//        var items = laborCostRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, LaborCostRepresentationDTO.class);
//        return items.stream().map(l -> (LaborCostRepresentation) l).toList();
    }

    @Override
    public int sizeInBackEnd(String filter, Pageable pageable) {
        var userDetails = getUserDetails();
        if (userDetails == null || userDetails.getAuthorities().isEmpty())
            return 0;
        return getCountItemsCountingUserRights(filter, userDetails.getUsername(), pageable);
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

    private String getQueryTextForLaborCosts() {
        return """
               SELECT :queryFields
               FROM labor_costs l
               JOIN labor_resources lr
                   ON l.labor_resource_id = lr.id
                   AND lr.name LIKE :search
               WHERE l.labor_resource_id IN(SELECT labor_resource_id
                                         FROM user_labor_resources
                                         WHERE user_id IN(SELECT id FROM users WHERE name = :userName)
                                        )
               :order
               LIMIT :limit OFFSET :offset
               """;
    }

    private List<LaborCostRepresentation> getItemsCountingUserRights(String filter, String userName, Pageable pageable) {
        var queryTextLaborCosts = this.queryTextLaborCosts.replace(":queryFields", "l.id, l.name, l.day, l.date_of_creation, lr.name resourceName");
        queryTextLaborCosts = queryTextLaborCosts.replace(":limit", String.valueOf(pageable.getPageSize()));
        queryTextLaborCosts = queryTextLaborCosts.replace(":offset", String.valueOf(pageable.getOffset()));
        queryTextLaborCosts = queryTextLaborCosts.replace(":order", "ORDER BY l.day");
        var query = entityManager.createNativeQuery(queryTextLaborCosts);
        query.setParameter("userName", userName);
        query.setParameter("search", "%" + filter + "%");
        return getLaborCostRepresentations(query);
    }

    private ArrayList<LaborCostRepresentation> getLaborCostRepresentations(Query query) {
        var resultList = (List<Object[]>) query.getResultList();
        var result = new ArrayList<LaborCostRepresentation>(resultList.size());
        for (var row: resultList) {
            var sqlDay = (java.sql.Date) row[2];
            var day = sqlDay.toLocalDate();
            var nameObj = row[1];
            var name = "";
            if (nameObj != null)
                name = (String) nameObj;
            var devNameObj = row[4];
            var devName = "";
            if (devNameObj != null)
                devName = (String) devNameObj;
            var rep = new LaborCostRepresentationDTO((Integer) row[0], name, day, (Date) row[3], devName);
            result.add(rep);
        }
        return result;
    }

    private int getCountItemsCountingUserRights(String filter, String userName, Pageable pageable) {
        var queryTextLaborCosts = this.queryTextLaborCosts.replace(":queryFields", "COUNT(l.id)");
        queryTextLaborCosts = queryTextLaborCosts.replace(":limit", String.valueOf(pageable.getPageSize()));
        queryTextLaborCosts = queryTextLaborCosts.replace(":offset", String.valueOf(pageable.getOffset()));
        queryTextLaborCosts = queryTextLaborCosts.replace(":order", "");
        var query = entityManager.createNativeQuery(queryTextLaborCosts);
        query.setParameter("userName", userName);
        query.setParameter("search", "%" + filter + "%");
        return ((Long) query.getSingleResult()).intValue();
    }

    private String getQueryTextForIntervalReps() {
        return """
                SELECT p.id, p.name
                FROM ProjectTaskImpl p
                WHERE
                p.id IN(:ids)
                """;
    }

    private UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;

        var principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

}
