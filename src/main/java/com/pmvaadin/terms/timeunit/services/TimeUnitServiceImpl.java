package com.pmvaadin.terms.timeunit.services;

import com.pmvaadin.commonobjects.services.ListService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitImpl;
import com.pmvaadin.terms.timeunit.repositories.TimeUnitRepositoryPaging;
import com.vaadin.flow.data.provider.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class TimeUnitServiceImpl implements TimeUnitService, ListService<TimeUnit, TimeUnit> {

    private TimeUnitRepositoryPaging timeUnitRepositoryPaging;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setTimeUnitRepositoryPaging(TimeUnitRepositoryPaging timeUnitRepositoryPaging) {
        this.timeUnitRepositoryPaging = timeUnitRepositoryPaging;
    }

    @Override
    public TimeUnit getPredefinedTimeUnit() {
        return timeUnitRepositoryPaging.findById(1).orElse(null);
    }

    @Override
    public List<TimeUnit> getPageByName(Query<TimeUnit, String> query) {

        return timeUnitRepositoryPaging.findByNameLikeIgnoreCase(
                "%" + query.getFilter().orElse("") + "%",
                PageRequest.of(query.getPage(), query.getPageSize()));

    }

    @Override
    public int getCountPageItemsByName(Query<TimeUnit, String> query) {
        return timeUnitRepositoryPaging.countByNameLikeIgnoreCase("%" + query.getFilter().orElse("") + "%");
    }

    @Override
    public <I> TimeUnit getTimeUnitById(I id) {
        return timeUnitRepositoryPaging.findById(id).get();
    }

    @Override
    public TimeUnit save(TimeUnit timeUnit) {
        return timeUnitRepositoryPaging.save(timeUnit);
    }

    // ListService
    @Override
    public List<TimeUnit> getItems(String filter, Pageable pageable) {

        return timeUnitRepositoryPaging.findByNameLikeIgnoreCase("%" + filter + "%", pageable);

    }

    @Override
    public int sizeInBackEnd(String filter, Pageable pageable) {
        return timeUnitRepositoryPaging.findByNameLikeIgnoreCase("%" + filter + "%", pageable).size();
    }

    @Override
    public TimeUnit add() {

        return new TimeUnitImpl();

    }

    @Override
    public TimeUnit get(TimeUnit representation) {
        return getTimeUnitById(representation.getId());
    }

    @Transactional
    @Override
    public boolean delete(Collection<TimeUnit> reps) {

        var ids = reps.stream().map(TimeUnit::getId).toList();
        var deletingIds = checkIfItemsCanBeDeleted(ids);

        timeUnitRepositoryPaging.deleteAllById(deletingIds);

        return true;

    }

    @Override
    public TimeUnit copy(TimeUnit calRep) {

        TimeUnit timeUnit = timeUnitRepositoryPaging.findById(calRep.getId()).orElse(new TimeUnitImpl());
        if (timeUnit instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) timeUnit).nullIdentifyingFields();

        return timeUnit;

    }

    private List<?> checkIfItemsCanBeDeleted(List<?> ids) {

        var reps = findUndeletableTimeUnits(ids);
        var checkPredefined = reps.stream().anyMatch(TimeUnit::isPredefined);

        if (checkPredefined) throw new StandardError("Cannot remove a predefined element");
        if (!reps.isEmpty()) {
            var string = reps.stream().map(c -> c.getName() + " with id " + c.getId()).toList().toString();
            throw new StandardError("Cannot remove the time units: " + string + ", because they is used in project tasks");
        }

        return reps.stream().map(TimeUnit::getId).toList();

    }

    private List<TimeUnit> findUndeletableTimeUnits(List<?> ids) {

        var queryText = getQueryTextForDetectionOfUndeletableTimeUnits();

        var idsParameter = String.valueOf(ids).replace("[", "'{").replace("]", "}'");
        queryText = queryText.replace(":ids", idsParameter);
        var query = entityManager.createNativeQuery(queryText, TimeUnitImpl.class);

        List<TimeUnitImpl> resultList = query.getResultList();

        return resultList.stream().map(t -> (TimeUnit) t).toList();

    }

    private String getQueryTextForDetectionOfUndeletableTimeUnits() {

        return """
            WITH predefined_time_units AS(
            SELECT
            	*
            FROM time_unit
            WHERE
            id = ANY(:ids)
            	AND predefined
            ),
                        
            used_time_units_ids AS(
            SELECT DISTINCT
            	time_unit_id id
            FROM project_tasks
            WHERE
            	time_unit_id = ANY(:ids)
           
            UNION ALL
            
            SELECT
            	time_unit_id id
            FROM links
            WHERE
            	time_unit_id = ANY(:ids)
            ),
                     
            used_time_units AS (
            SELECT
            	*
            FROM time_unit
            WHERE
            	id IN(SELECT id FROM used_time_units_ids)
            )
                        
            SELECT DISTINCT
            	*
            FROM predefined_time_units
                        
            UNION
                        
            SELECT
            	*
            FROM used_time_units
            """;

    }

}
