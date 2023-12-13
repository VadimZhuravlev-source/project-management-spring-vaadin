package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.repositories.CalendarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarServiceTransactionalImpl {

    private CalendarRepository calendarRepository;

    @Autowired
    public void setCalendarRepository(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Transactional
    public Calendar save(Calendar calendar) {
        return calendarRepository.save(calendar);
    }

}
