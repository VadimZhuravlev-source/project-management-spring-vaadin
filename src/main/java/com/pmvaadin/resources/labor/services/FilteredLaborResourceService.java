package com.pmvaadin.resources.labor.services;

import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.resources.labor.entity.LaborResourceRepresentationDTO;
import com.pmvaadin.security.services.UserService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FilteredLaborResourceService extends LaborResourceServiceImpl {

    private UserService userService;
    private EntityManager entityManager;
    private final String queryText = getQueryText();

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<LaborResourceRepresentation> getItems(String filter, Pageable pageable) {
        return getUserResources(filter, pageable);
    }

    @Override
    public int sizeInBackEnd(String filter, Pageable pageable) {
        // TODO getting of items count in the page in the query
        return getUserResources(filter, pageable).size();
    }

    private String getQueryText() {
        return """
                SELECT DISTINCT
                    labor_resources.id,
                    labor_resources.name
                FROM
                    user_labor_resources ur
                    JOIN labor_resources
                        ON ur.labor_resource_id = labor_resources.id
                        AND labor_resources.name LIKE :filter
                    JOIN users u
                        ON ur.user_id = u.id
                        AND u.name = :userName
                LIMIT &limit OFFSET &offset
                """;
    }

    private <I> List<LaborResourceRepresentation> getUserResources(String filter, Pageable pageable) {
        var userName = userService.getUserName();
        if (userName == null)
            return new ArrayList<>(0);

        var queryText = this.queryText.replace("&limit", String.valueOf(pageable.getPageSize()));
        queryText = queryText.replace("&offset", String.valueOf(pageable.getOffset()));

        var query = entityManager.createNativeQuery(queryText);
        query.setParameter("userName", userName);
        query.setParameter("filter", "%" + filter + "%");
        var result = new ArrayList<LaborResourceRepresentation>();
        var list = (List<Object[]>) query.getResultList();
        for (var row: list) {
            var resource = new LaborResourceRepresentationDTO((Integer) row[0], row[1].toString());
            result.add(resource);
        }
        return result;
//        return query.getResultStream().map(row -> new LaborResourceRepresentationDTO(Integer.valueOf(row[0]), String.valueOf(row[1]))).toList();
    }

}
