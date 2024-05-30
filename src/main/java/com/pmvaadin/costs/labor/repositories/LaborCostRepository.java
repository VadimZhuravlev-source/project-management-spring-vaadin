package com.pmvaadin.costs.labor.repositories;

import com.pmvaadin.costs.labor.entities.LaborCost;
import com.pmvaadin.costs.labor.entities.LaborCostImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LaborCostRepository extends Repository<LaborCostImpl, Integer> {

    <T> List<T> findByNameLikeIgnoreCase(String name, Pageable pageable, Class<T> type);

    <I> Optional<LaborCost> findById(I i);

    void deleteAllById(Iterable<?> ids);

    LaborCost save(LaborCost laborResource);

    <T> List<T> findAllByIdIn(@Param("id") Iterable<?> ids, Class<T> type);

    @Query(value = """
            SELECT p.id, p.name
            FROM ProjectTaskImpl p
            WHERE
            CAST(p.startDate AS DATE) <= :day
            AND CAST(p.finishDate AS DATE) >= :day
            AND id IN(
            SELECT tr.projectTaskId
            FROM TaskResourceImpl tr
            WHERE tr.resourceId = :resourceId)
            """)
    <I, T> List<T> getAvailableTasks(@Param("resourceId") I resourceId, @Param("day") LocalDate day, Class<T> type);

}
