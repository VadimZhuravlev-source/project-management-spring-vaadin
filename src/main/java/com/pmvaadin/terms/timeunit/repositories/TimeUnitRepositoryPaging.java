package com.pmvaadin.terms.timeunit.repositories;

import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TimeUnitRepositoryPaging extends Repository<TimeUnitImpl, Integer> {

    <T> List<T> findByNameLikeIgnoreCase(String name, Pageable pageable, Class<T> type);
    int countByNameLikeIgnoreCase(String name);

    <I> Optional<TimeUnit> findById(I i);

    List<TimeUnit> findAllById(Iterable<?> ids);

    void deleteAllById(Iterable<?> ids);

    TimeUnit save(TimeUnit timeUnit);

}
