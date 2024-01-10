package com.pmvaadin.resources.repositories;

import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.resources.entity.LaborResourceImpl;
import com.pmvaadin.resources.entity.LaborResourceRepresentation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LaborResourceRepository extends Repository<LaborResourceImpl, Integer> {

    <T> List<T> findByNameLikeIgnoreCase(String name, Pageable pageable, Class<T> type);

    <I> Optional<LaborResource> findById(I i);

    void deleteAllById(Iterable<?> ids);

    LaborResource save(LaborResource laborResource);

    <T> List<T> findAllByIdIn(@Param("id") Iterable<?> ids, Class<T> type);

}
