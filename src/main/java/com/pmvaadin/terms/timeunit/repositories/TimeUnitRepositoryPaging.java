package com.pmvaadin.terms.timeunit.repositories;

import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface TimeUnitRepositoryPaging extends PagingAndSortingRepository<TimeUnitImpl, Integer> {

    List<TimeUnit> findByNameLikeIgnoreCase(String name, Pageable pageable);
    int countByNameLikeIgnoreCase(String name);

    Optional<TimeUnit> findById(int i);

    List<TimeUnit> findAllById(Iterable<?> ids);

}
