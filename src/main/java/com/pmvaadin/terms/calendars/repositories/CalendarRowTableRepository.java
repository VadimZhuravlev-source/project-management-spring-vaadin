package com.pmvaadin.terms.calendars.repositories;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CalendarRowTableRepository extends Repository<CalendarImpl, Integer> {

    List<Calendar> findAll();
    Calendar findById(Integer id);
}
