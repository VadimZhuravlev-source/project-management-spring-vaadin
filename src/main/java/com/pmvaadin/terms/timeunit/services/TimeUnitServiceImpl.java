package com.pmvaadin.terms.timeunit.services;

import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.repositories.TimeUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeUnitServiceImpl implements TimeUnitService{

    private TimeUnitRepository timeUnitRepository;

    @Autowired
    public void setTimeUnitRepository(TimeUnitRepository timeUnitRepository) {
        this.timeUnitRepository = timeUnitRepository;
    }

    @Override
    public TimeUnit getPredefinedTimeUnit() {
        return timeUnitRepository.findById(1).orElse(null);
    }

}
