package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.calendar.CalendarImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CalendarRowTableRepository extends Repository<CalendarImpl, Integer> {

    List<CalendarImpl> findAll();
    CalendarImpl findById(Integer id);
}
