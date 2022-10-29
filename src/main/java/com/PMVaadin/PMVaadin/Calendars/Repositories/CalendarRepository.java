package com.PMVaadin.PMVaadin.Calendars.Repositories;

import com.PMVaadin.PMVaadin.Calendars.Entity.Calendar;
import com.PMVaadin.PMVaadin.Calendars.Entity.CalendarImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends Repository<CalendarImpl, Integer> {

    List<Calendar> findAll();
    Optional<Calendar> findById(Integer id);

    Calendar save(Calendar calendar);
    void deleteAllById(Iterable<?> ids);
    void deleteById(Integer id);

}
