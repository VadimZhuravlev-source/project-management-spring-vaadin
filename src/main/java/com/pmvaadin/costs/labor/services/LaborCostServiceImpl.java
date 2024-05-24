package com.pmvaadin.costs.labor.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.LaborCostImpl;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentation;
import com.pmvaadin.costs.labor.entities.LaborCostRepresentationDTO;
import com.pmvaadin.costs.labor.repositories.LaborCostRepository;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class LaborCostServiceImpl implements LaborCostService, ListService<LaborCostRepresentation, LaborCost> {

    private LaborCostRepository laborCostRepository;
    private EntityManager entityManager;

    @Autowired
    public void setLaborCostRepository(LaborCostRepository laborCostRepository) {
        this.laborCostRepository = laborCostRepository;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
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

        var ids = reps.stream().map(LaborCostRepresentation::getId).toList();
        var deletingIds = checkIfItemsCanBeDeleted(ids);

        laborCostRepository.deleteAllById(deletingIds);

        return true;

    }

    @Override
    public LaborCost copy(LaborCostRepresentation calRep) {

        LaborCost laborCost = laborCostRepository.findById(calRep.getId()).orElse(new LaborCostImpl());
        if (laborCost instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) laborCost).nullIdentifyingFields();

        return laborCost;

    }

    private List<?> checkIfItemsCanBeDeleted(List<?> ids) {

        var reps = findUndeletableLaborCosts(ids);
        if (!reps.isEmpty()) {
            var string = reps.stream().map(c -> c.getName() + " with id " + c.getId()).toList().toString();
            throw new StandardError("Cannot remove the labor resources: " + string + ", because they is used in project tasks");
        }

        return reps.stream().map(LaborCost::getId).toList();

    }

    private List<LaborCost> findUndeletableLaborCosts(List<?> ids) {

        var queryText = getQueryTextForDetectionOfUndeletableLaborCosts();

        var idsParameter = String.valueOf(ids).replace("[", "'{").replace("]", "}'");
        queryText = queryText.replace(":ids", idsParameter);
        var query = entityManager.createNativeQuery(queryText, LaborCostImpl.class);

        List<LaborCostImpl> resultList = query.getResultList();

        return resultList.stream().map(t -> (LaborCost) t).toList();

    }

    private String getQueryTextForDetectionOfUndeletableLaborCosts() {

        return """
            WITH used_labor_resources AS(
                SELECT
                    resource_id
                FROM task_labor_resources
                WHERE
                    resource_id = ANY(:ids)
            )
            
            SELECT
                *
            FROM labor_resources
                JOIN used_labor_resources
                    ON labor_resources.id = used_labor_resources.resource_id
        """;

    }


}
