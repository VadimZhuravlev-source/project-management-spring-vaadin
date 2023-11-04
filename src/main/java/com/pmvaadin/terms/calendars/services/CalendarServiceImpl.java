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

import javax.transaction.Transactional;
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

    @Transactional
    @Override
    public void saveCalendars(Calendar calendar) {

        calendar.getDaysOfWeekSettings().forEach(dayOfWeekSettings -> {
            dayOfWeekSettings.setCalendar((CalendarImpl) calendar);
                });

        calendar.getCalendarException().forEach(exceptionDays -> {
            exceptionDays.setCalendar((CalendarImpl) calendar);
        });

        calendarRepository.save(calendar);


    }

    @Transactional
    @Override
    public void deleteCalendar(Calendar calendar) {

        var ids = new ArrayList<>(1);
        ids.add(calendar.getId());
        var deletingIds = checkIfItemsCanBeDeleted(ids);
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

    @Transactional
    @Override
    public boolean delete(Collection<CalendarRepresentation> calReps) {

        var ids = calReps.stream().map(CalendarRepresentation::getId).toList();
        var deletingIds = checkIfItemsCanBeDeleted(ids);

        calendarRepository.deleteAllById(deletingIds);

        return true;

    }

    @Override
    public Calendar copy(CalendarRepresentation calRep) {

        Calendar calendar = calendarRepository.findById(calRep.getId()).orElse(defaultCalendar.getDefaultCalendar());
        calendar.setId(null);
        calendar.setVersion(null);
        calendar.setPredefined(false);
        calendar.getDaysOfWeekSettings().forEach(dayOfWeekSettings -> {
            dayOfWeekSettings.setId(null);
        });
        calendar.getCalendarException().forEach(exceptionDay -> {
            exceptionDay.setId(null);
        });

        return calendar;

    }

    @Override
    public Calendar getCalendar(CalendarRepresentation calendarRep) {

        var id = calendarRep.getId();
        var calendar = calendarRepository.findById(id);
        if (calendar.isEmpty()) throw new StandardError("The calendar has been deleted by another user.");
        return calendar.get();

    }

    private List<?> checkIfItemsCanBeDeleted(List<?> ids) {

        var calendarReps = calendarRepository.findCalendarsThatCannotBeDeleted(ids, CalendarRepresentationDTO.class);

        var checkPredefined = calendarReps.stream().anyMatch(CalendarRepresentationDTO::isPredefined);

        if (checkPredefined) throw new StandardError("Cannot remove a predefined element");
        if (!calendarReps.isEmpty()) {
            var calendarsString = calendarReps.stream().map(c -> c.getName() + " with id " + c.getId()).toList().toString();
            throw new StandardError("Cannot remove the calendars: " + calendarsString + ", because they is used in project tasks");
        }

        var foundCalendars = calendarRepository.findAllByIdIn(ids, CalendarRepresentationDTO.class);

        return foundCalendars.stream().map(CalendarRepresentationDTO::getId).toList();

    }

}
