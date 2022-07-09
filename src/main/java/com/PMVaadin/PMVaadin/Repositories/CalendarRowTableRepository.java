package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.Calendar.Calendar;
import com.PMVaadin.PMVaadin.Entities.Calendar.CalendarRowTable;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CalendarRowTableRepository extends Repository<Calendar, Integer> {

    List<CalendarRowTable> findAll();

}
