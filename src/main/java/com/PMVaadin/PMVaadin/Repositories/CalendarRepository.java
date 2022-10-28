package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.calendar.Calendar;
import com.PMVaadin.PMVaadin.Entities.calendar.CalendarImpl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<CalendarImpl, Integer> {

    List<CalendarImpl> findAll();
    @Override
    Optional<CalendarImpl> findById(Integer id);
}
