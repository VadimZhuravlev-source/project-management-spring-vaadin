package com.pmvaadin.terms.calendars.repositories;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends Repository<CalendarImpl, Integer> {

    List<Calendar> findAll();
    List<Calendar> findAllById(Iterable<?> ids);
    <I> Optional<Calendar> findById(I id);
    Calendar save(Calendar calendar);
    void deleteAllById(Iterable<?> ids);
    void deleteById(Integer id);

}
