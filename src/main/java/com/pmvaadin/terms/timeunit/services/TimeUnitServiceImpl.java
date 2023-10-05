package com.pmvaadin.terms.timeunit.services;

import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.repositories.TimeUnitRepositoryPaging;
import com.vaadin.flow.data.provider.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
    public List<TimeUnit> getPageByName(Query<TimeUnit, String> query) {

        return timeUnitRepositoryPaging.findByNameLikeIgnoreCase(
                "%" + query.getFilter().orElse("") + "%",
                PageRequest.of(query.getPage(), query.getPageSize()));

    }

    @Override
    public int getCountPageItemsByName(Query<TimeUnit, String> query) {
        return timeUnitRepositoryPaging.countByNameLikeIgnoreCase("%" + query.getFilter().orElse("") + "%");
    }

    @Override
    public TimeUnit getTimeUnitById(Integer id) {
        return timeUnitRepositoryPaging.findById(id).get();
    }

}
