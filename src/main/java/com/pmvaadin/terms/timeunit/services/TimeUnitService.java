package com.pmvaadin.terms.timeunit.services;

import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TimeUnitService {

    TimeUnit getPredefinedTimeUnit();
    List<TimeUnit> getPageByName(String name, Pageable pageable);
    int getCountPageItemsByName(String name);

    TimeUnit getTimeUnitById(Integer id);

}
