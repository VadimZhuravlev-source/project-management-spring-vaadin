package com.PMVaadin.PMVaadin.Calendars.Repositories;

import com.PMVaadin.PMVaadin.Calendars.Entity.Calendar;
import com.PMVaadin.PMVaadin.Calendars.Entity.CalendarImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CalendarRowTableRepository extends Repository<CalendarImpl, Integer> {

    List<Calendar> findAll();
    Calendar findById(Integer id);
}
