package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import com.pmvaadin.terms.calendars.repositories.CalendarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TermCalculationServiceImpl implements TermCalculationService {

    // used in CalendarServiceImpl
    private final Calendar defaultCalendar = new CalendarImpl().getDefaultCalendar();
    private CalendarRepository calendarRepository;

    @Autowired
    public void setCalendarRepository(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    public Calendar getDefaultCalendar() {
        return calendarRepository.findById(1).orElse(defaultCalendar);
    }

    @Override
    public void fillCalendars(TermCalculationData termCalculationData) {

        var calendarIds = termCalculationData.getProjectTasks().stream()
                .map(ProjectTask::getCalendarId).toList();

        List<Calendar> calendars = calendarRepository.findAllById(calendarIds);

        var defaultCalendar = getDefaultCalendar();
        termCalculationData.setDefaultCalendar(defaultCalendar);
        termCalculationData.setCalendars(calendars);

    }

}
