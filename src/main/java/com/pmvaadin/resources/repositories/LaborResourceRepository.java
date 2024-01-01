package com.pmvaadin.resources.repositories;

import com.pmvaadin.resources.entity.LaborResource;
import com.pmvaadin.resources.entity.LaborResourceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface LaborResourceRepository extends PagingAndSortingRepository<LaborResourceImpl, Integer> {

    List<LaborResource> findByNameLikeIgnoreCase(String name, Pageable pageable);

    <I> Optional<LaborResource> findById(I i);

    void deleteAllById(Iterable<?> ids);

    LaborResource save(LaborResource laborResource);

}
