package com.pmvaadin.terms.timeunit.services;

import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.repositories.TimeUnitRepositoryPaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeUnitServiceImpl implements TimeUnitService{

    //private TimeUnitRepository timeUnitRepository;
    private TimeUnitRepositoryPaging timeUnitRepositoryPaging;

//    @Autowired
//    public void setTimeUnitRepository(TimeUnitRepository timeUnitRepository) {
//        this.timeUnitRepository = timeUnitRepository;
//    }

    @Autowired
    public void setTimeUnitRepositoryPaging(TimeUnitRepositoryPaging timeUnitRepositoryPaging) {
        this.timeUnitRepositoryPaging = timeUnitRepositoryPaging;
    }

    @Override
    public TimeUnit getPredefinedTimeUnit() {
        return timeUnitRepositoryPaging.findById(1).orElse(null);
    }

    @Override
    public List<TimeUnit> getPageByName(String name, Pageable pageable) {
        return timeUnitRepositoryPaging.findByNameLikeIgnoreCase("%" + name + "%", pageable);
    }

    @Override
    public int getCountPageItemsByName(String name) {
        return timeUnitRepositoryPaging.countByNameLikeIgnoreCase("%" + name + "%");
    }

    @Override
    public TimeUnit getTimeUnitById(Integer id) {
        return timeUnitRepositoryPaging.findById(id).get();
    }

}
