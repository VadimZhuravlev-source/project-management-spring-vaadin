package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.Calendar.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar, Integer> {

    List<Calendar> findAll();
    @Override
    Optional<Calendar> findById(Integer id);
}
