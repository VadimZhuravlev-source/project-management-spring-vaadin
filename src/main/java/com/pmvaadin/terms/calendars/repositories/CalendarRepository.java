package com.pmvaadin.terms.calendars.repositories;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends Repository<CalendarImpl, Integer> {

    List<Calendar> findAllById(Iterable<?> ids);
    <I> Optional<Calendar> findById(I id);
    Calendar save(Calendar calendar);
    void deleteAllById(Iterable<?> ids);
    <T> List<T> findAllByIdIn(@Param("id") Iterable<?> ids, Class<T> type);

    <T> List<T> findByNameLikeIgnoreCase(String name, Pageable pageable, Class<T> type);
//    int countByNameLikeIgnoreCase(String name, Pageable pageable);

}
