package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.commonobjects.services.ListService;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.entity.*;
import com.pmvaadin.terms.calendars.repositories.CalendarRepository;
import com.pmvaadin.terms.calculation.TermCalculationData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService, ListService<CalendarRepresentation, Calendar> {

    private final Calendar defaultCalendar = new CalendarImpl().getDefaultCalendar();

    private CalendarRepository calendarRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public void setCalendarRepository(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    public <I> Calendar getCalendarById(I id) {

        var calendarDataBaseOpt = calendarRepository.findById(id);
        if (calendarDataBaseOpt.isEmpty())
            throw new StandardError("The calendar has been deleted by another user.");

        return calendarDataBaseOpt.get();

    }

    @Override
    public Calendar getDefaultCalendar() {
        return calendarRepository.findById(1).orElse(defaultCalendar);
    }

    @Transactional
    @Override
    public void saveCalendar(Calendar calendar) {

        if (!calendar.isNew()) {

            var id = calendar.getId();
            var calendarDataBaseOpt = calendarRepository.findById(id);
            if (calendarDataBaseOpt.isEmpty())
                throw new StandardError("The calendar has been deleted by another user.");
            var calendarDataBase = calendarDataBaseOpt.get();
            if (!calendarDataBase.getVersion().equals(calendar.getVersion()))
                throw new StandardError("The calendar has been changed by another user.");
        }

        // TODO to define alterations of the DaysOfWeekSettings and the CalendarException and to find tasks that used this calendar
        calendar.getDaysOfWeekSettings().forEach(dayOfWeekSettings ->
            dayOfWeekSettings.setCalendar((CalendarImpl) calendar)
                );

        calendar.getCalendarException().forEach(exceptionDays ->
            exceptionDays.setCalendar((CalendarImpl) calendar)
        );

        calendarRepository.save(calendar);


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
    public Calendar get(CalendarRepresentation representation) {
        return getCalendarById(representation.getId());
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
        calendar.getDaysOfWeekSettings().forEach(dayOfWeekSettings ->
            dayOfWeekSettings.setId(null)
        );
        calendar.getCalendarException().forEach(exceptionDay ->
            exceptionDay.setId(null)
        );

        return calendar;

    }

    private List<?> checkIfItemsCanBeDeleted(List<?> ids) {

        var calendarReps = findCalendarDTOForDetectionOfUndeletableCalendars(ids);

        var checkPredefined = calendarReps.stream().anyMatch(CalendarRepresentation::isPredefined);

        if (checkPredefined) throw new StandardError("Cannot remove a predefined element");
        if (!calendarReps.isEmpty()) {
            var calendarsString = calendarReps.stream().map(c -> c.getName() + " with id " + c.getId()).toList().toString();
            throw new StandardError("Cannot remove the calendars: " + calendarsString + ", because they is used in project tasks");
        }

        var foundCalendars = calendarRepository.findAllByIdIn(ids, CalendarRepresentationDTO.class);

        return foundCalendars.stream().map(CalendarRepresentationDTO::getId).toList();

    }

    private List<CalendarRepresentation> findCalendarDTOForDetectionOfUndeletableCalendars(List<?> ids) {

        var queryText = getQueryTextForDetectionOfUndeletableCalendars();

        var idsParameter = String.valueOf(ids).replace("[", "'{").replace("]", "}'");
        queryText = queryText.replace(":ids", idsParameter);
        var query = entityManager.createNativeQuery(queryText);
        //query.setParameter("ids", idsParameter);

        List<Object[]> resultList = query.getResultList();

        var calendarReps = new ArrayList<CalendarRepresentation>(resultList.size());

        var converter = applicationContext.getBean(CalendarSettingsConverter.class);

        for (Object[] row: resultList) {

            CalendarSettings calendarSettings = converter.convertToEntityAttribute(Integer.valueOf((Short) row[2]));
            var time = (Time) row[3];
            LocalTime localTime = null;
            if (time != null) localTime = time.toLocalTime();
            var dto = new CalendarRepresentationDTO((Integer) row[0], (String) row[1], calendarSettings, localTime, (Boolean) row[4]);
            calendarReps.add(dto);

        }

        return calendarReps;

    }

    private String getQueryTextForDetectionOfUndeletableCalendars() {

        var text = """
            WITH predefined_calendars AS(
            SELECT
            	id,
            	name,
            	settings_id,
            	start_time,
            	predefined
            FROM calendars
            WHERE
            id = ANY(:ids)
            	AND predefined
            ),
                        
            used_calendars_ids AS(
            SELECT DISTINCT
            	calendar_id id
            FROM project_tasks
            WHERE
            	calendar_id = ANY(:ids)
            ),
                        
            used_calendars AS (
            SELECT
            	id,
            	name,
            	settings_id,
            	start_time,
            	predefined
            FROM calendars
            WHERE
            	id IN(SELECT id FROM used_calendars_ids)
            )
                        
            SELECT DISTINCT
            	*
            FROM predefined_calendars
                        
            UNION
                        
            SELECT
            	*
            FROM used_calendars
            """;

        return text;

    }

}
