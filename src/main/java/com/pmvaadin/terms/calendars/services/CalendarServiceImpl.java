package com.pmvaadin.terms.calendars.services;

import com.pmvaadin.commonobjects.services.ListService;
import com.pmvaadin.projectstructure.ProjectRecalculation;
import com.pmvaadin.projectstructure.StandardError;
import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.entity.*;
import com.pmvaadin.terms.calendars.repositories.CalendarRepository;
import com.pmvaadin.terms.calendars.validators.CalendarValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService, ListService<CalendarRepresentation, Calendar> {

    // used in CalendarServiceImpl
    private final Calendar defaultCalendar = new CalendarImpl().getDefaultCalendar();

    private CalendarRepository calendarRepository;
    private ProjectRecalculation projectRecalculation;

    private CalendarServiceTransactionalImpl calServiceTran;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public void setCalendarRepository(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Autowired
    public void setProjectRecalculation(ProjectRecalculation projectRecalculation) {
        this.projectRecalculation = projectRecalculation;
    }

    @Autowired
    public void setCalServiceTran(CalendarServiceTransactionalImpl calServiceTran) {
        this.calServiceTran = calServiceTran;
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

    @Override
    public Calendar save(Calendar calendar) {

        var calendarValidation = applicationContext.getBean(CalendarValidation.class);
        var proceed = calendarValidation.validate(calendar);
        if (!proceed) return calendar;

        if (!calendar.isNew()) {

            var id = calendar.getId();
            var calendarDataBaseOpt = calendarRepository.findById(id);
            if (calendarDataBaseOpt.isEmpty())
                throw new StandardError("The calendar has been deleted by another user.");
            var calendarDataBase = calendarDataBaseOpt.get();
            if (!calendarDataBase.getVersion().equals(calendar.getVersion()))
                throw new StandardError("The calendar has been changed by another user.");

        }

        calendar.fillWorkingWeekSort();
        calendar.fillExceptionSort();

        var savedCalendar = calServiceTran.save(calendar);

        projectRecalculation.recalculate(savedCalendar, calendar);

        return savedCalendar;

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
        if (calendar instanceof HasIdentifyingFields)
            ((HasIdentifyingFields) calendar).nullIdentifyingFields();

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

        List<Object[]> resultList = query.getResultList();

        var calendarReps = new ArrayList<CalendarRepresentation>(resultList.size());

        var converter = applicationContext.getBean(CalendarSettingsConverter.class);

        for (Object[] row: resultList) {

            CalendarSettings calendarSettings = converter.convertToEntityAttribute(Integer.valueOf((Short) row[2]));
            var dto = new CalendarRepresentationDTO((Integer) row[0], (String) row[1], calendarSettings, (Boolean) row[4]);
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
