package com.pmvaadin.calendars.repositories;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.calendars.entity.CalendarImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CalendarRowTableRepository extends Repository<CalendarImpl, Integer> {

    List<Calendar> findAll();
    Calendar findById(Integer id);
}
