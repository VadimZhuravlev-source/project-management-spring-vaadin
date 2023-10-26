package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.commonobjects.services.ListService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import com.pmvaadin.terms.calendars.entity.CalendarRepresentation;
import com.pmvaadin.terms.calendars.entity.CalendarRepresentationDTO;
import com.pmvaadin.terms.calendars.repositories.CalendarRepository;
import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService, ListService<CalendarRepresentation, Calendar> {

    private final Calendar defaultCalendar = new CalendarImpl().getDefaultCalendar();

    private CalendarRepository calendarRepository;

    @Autowired
    public void setCalendarRepository(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    public List<Calendar> getCalendars() {
        return calendarRepository.findAll();
    }

    @Override
    public <I> Calendar getCalendarById(I id) {

        return calendarRepository.findById(id).orElse(null);

    }

    @Override
    public Calendar getDefaultCalendar() {
        return calendarRepository.findById(1).orElse(defaultCalendar);
    }

    @Override
    public void saveCalendars(Calendar calendar) {
        calendarRepository.save(calendar);
    }

    @Override
    public void deleteCalendar(Calendar calendar) {

        var ids = new ArrayList<>(1);
        ids.add(calendar.getId());
        var deletingIds = checkPredefinedElementInListOfIds(ids);
        calendarRepository.deleteAllById(deletingIds);

    }

    @Override
    public void fillCalendars(TermCalculationData termCalculationData) {

        var calendarIds = termCalculationData.getProjectTasks().stream()
                .map(ProjectTask::getCalendarId).toList();

        List<Calendar> calendars = calendarRepository.findAllById(calendarIds);

        termCalculationData.setDefaultCalendar(defaultCalendar);
        termCalculationData.setCalendars(calendars);

    }

    // ListService
    @Override
    public List<CalendarRepresentation> getItems(String filter, Pageable pageable) {

        var list = calendarRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, CalendarRepresentationDTO.class);

        return list.stream().map(c -> (CalendarRepresentation) c).toList();

    }

    @Override
    public int sizeInBackEnd(String filter, Pageable pageable) {
        return calendarRepository.findByNameLikeIgnoreCase("%" + filter + "%", pageable, CalendarRepresentationDTO.class).size();
    }

    @Override
    public Calendar add() {

        return defaultCalendar.getDefaultCalendar();

    }

    @Override
    public boolean delete(Collection<CalendarRepresentation> calReps) {

        var ids = calReps.stream().map(CalendarRepresentation::getId).toList();
        var deletingIds = checkPredefinedElementInListOfIds(ids);

        calendarRepository.deleteAllById(deletingIds);

        return true;

    }

    @Override
    public Calendar copy(CalendarRepresentation calRep) {
        return calendarRepository.findById(calRep.getId()).orElse(defaultCalendar.getDefaultCalendar());
    }

    private List<?> checkPredefinedElementInListOfIds(List<?> ids) {

        var foundCalendars = calendarRepository.findAllById(ids);
        var checkPredefined = foundCalendars.stream().anyMatch(Calendar::isPredefined);

        if (checkPredefined) throw new StandardError("Cannot remove a predefined element");

        return foundCalendars.stream().map(Calendar::getId).toList();

    }

}
