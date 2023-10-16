package com.pmvaadin.terms.timeunit.repositories;

import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitImpl;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TimeUnitRepository {//extends Repository<TimeUnitImpl, Integer> {

    <I> Optional<TimeUnit> findById(I id);
    List<TimeUnit> findAllById(Iterable<?> ids);

}
