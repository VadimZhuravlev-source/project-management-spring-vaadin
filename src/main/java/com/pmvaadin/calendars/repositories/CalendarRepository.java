package com.pmvaadin.calendars.repositories;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.calendars.entity.CalendarImpl;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends Repository<CalendarImpl, Integer> {

    List<Calendar> findAll();
    List<Calendar> findAllById(Iterable<?> ids);
    Optional<Calendar> findById(Integer id);
    Calendar save(Calendar calendar);
    void deleteAllById(Iterable<?> ids);
    void deleteById(Integer id);

}
