package com.pmvaadin.resources.labor.services;

import com.pmvaadin.common.services.ListService;
import com.pmvaadin.project.structure.StandardError;
import com.pmvaadin.resources.labor.entity.LaborResource;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentationDTO;
import com.pmvaadin.resources.labor.entity.LaborResourceImpl;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.repositories.LaborResourceRepository;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service(value = "LaborResourceService")
public class LaborResourceServiceImpl implements LaborResourceService, ListService<LaborResourceRepresentation, LaborResource> {

    private LaborResourceRepository laborResourceRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setLaborResourceRepository(LaborResourceRepository laborResourceRepository) {
        this.laborResourceRepository = laborResourceRepository;
    }

    @Override
    public <I> LaborResourceRepresentation getById(I id) {
        var ids = new ArrayList<>();
        ids.add(id);
        return laborResourceRepository.findAllByIdIn(ids, LaborResourceRepresentationDTO.class).stream().findFirst().orElse(null);
    }

    @Override
    public <I> List<LaborResourceRepresentation> getAllById(Collection<I> ids) {
        return laborResourceRepository.findAllByIdIn(ids, LaborResourceRepresentationDTO.class).stream()
                .map(l -> (LaborResourceRepresentation) l).collect(Collectors.toList());
    }

    // ListService
    @Override
    public LaborResource save(LaborResource laborResource) {
        return laborResourceRepository.save(laborResource);
    }

    @Override
    public List<LaborResourceRepresentation> getItems(String filter, Pageable pageable) {
        var items = laborResourceRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, LaborResourceRepresentationDTO.class);
        return items.stream().map(l -> (LaborResourceRepresentation) l).toList();
    }

    @Override
    public int sizeInBackEnd(String filter, Pageable pageable) {
        return laborResourceRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, LaborResourceRepresentationDTO.class).size();
    }

    @Override
    public LaborResource add() {

        return new LaborResourceImpl();

    }

    @Override
    public LaborResource get(LaborResourceRepresentation representation) {
        return laborResourceRepository.findById(representation.getId()).get();
    }

    @Transactional
    @Override
    public boolean delete(Collection<LaborResourceRepresentation> reps) {

        var ids = reps.stream().map(LaborResourceRepresentation::getId).toList();
        var deletingIds = checkIfItemsCanBeDeleted(ids);

        laborResourceRepository.deleteAllById(deletingIds);

        return true;

    }

    @Override
    public LaborResource copy(LaborResourceRepresentation calRep) {

        LaborResource laborResource = laborResourceRepository.findById(calRep.getId()).orElse(new LaborResourceImpl());
        if (laborResource instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) laborResource).nullIdentifyingFields();

        return laborResource;

    }

    private List<?> checkIfItemsCanBeDeleted(List<?> ids) {

        var reps = findUndeletableLaborResources(ids);
        if (!reps.isEmpty()) {
            var string = reps.stream().map(c -> c.getName() + " with id " + c.getId()).toList().toString();
            throw new StandardError("Cannot remove the labor resources: " + string + ", because they is used in project tasks");
        }

        return reps.stream().map(LaborResource::getId).toList();

    }

    private List<LaborResource> findUndeletableLaborResources(List<?> ids) {

        var queryText = getQueryTextForDetectionOfUndeletableLaborResources();

        var idsParameter = String.valueOf(ids).replace("[", "'{").replace("]", "}'");
        queryText = queryText.replace(":ids", idsParameter);
        var query = entityManager.createNativeQuery(queryText, LaborResourceImpl.class);

        List<LaborResourceImpl> resultList = query.getResultList();

        return resultList.stream().map(t -> (LaborResource) t).toList();

    }

    private String getQueryTextForDetectionOfUndeletableLaborResources() {

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
