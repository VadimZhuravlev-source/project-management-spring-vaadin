package com.pmvaadin.terms.timeunit.services;

import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.vaadin.flow.data.provider.Query;

import java.util.List;

public interface TimeUnitService {

    TimeUnit getPredefinedTimeUnit();
    List<TimeUnit> getPageByName(Query<TimeUnit, String> query);
    int getCountPageItemsByName(Query<TimeUnit, String> query);
    <I> TimeUnit getTimeUnitById(I id);
    TimeUnit save(TimeUnit timeUnit);

}
