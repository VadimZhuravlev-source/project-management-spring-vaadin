package com.pmvaadin.costs.labor.repositories;

import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.resources.labor.entity.LaborResourceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LaborCostRepository extends Repository<LaborResourceImpl, Integer> {

    <T> List<T> findByNameLikeIgnoreCase(String name, Pageable pageable, Class<T> type);

    <I> Optional<LaborCost> findById(I i);

    void deleteAllById(Iterable<?> ids);

    LaborCost save(LaborCost laborResource);

    <T> List<T> findAllByIdIn(@Param("id") Iterable<?> ids, Class<T> type);

}
