package com.pmvaadin.calendars.services;

import com.pmvaadin.calendars.entity.Calendar;
import com.pmvaadin.calendars.entity.CalendarImpl;
import com.pmvaadin.calendars.repositories.CalendarRepository;
import com.pmvaadin.calendars.repositories.CalendarRowTableRepository;
import com.pmvaadin.calendars.repositories.DayOfWeekSettingsRepository;
import com.pmvaadin.projectstructure.termscalculation.TermCalculationData;
import com.pmvaadin.projecttasks.entity.ProjectTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    private Calendar defaultCalendar = new CalendarImpl().getDefaultCalendar();

    private CalendarRepository calendarRepository;
    private CalendarRowTableRepository calendarRowTableRepository;
    private DayOfWeekSettingsRepository dayOfWeekSettingsRepository;
    private List<Calendar> calendarTableList;

    @Autowired
    public void setCalendarRepository(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Autowired
    public void setCalendarRowTableRepository(CalendarRowTableRepository calendarRowTableRepository) {
        this.calendarRowTableRepository = calendarRowTableRepository;
    }

    @Autowired
    public void setDayOfWeekSettingsRepository(DayOfWeekSettingsRepository dayOfWeekSettingsRepository) {
        this.dayOfWeekSettingsRepository = dayOfWeekSettingsRepository;
    }

    @Override
    public List<Calendar> getCalendars() {
        return calendarRowTableRepository.findAll();
    }

    @Override
    public Calendar getCalendarById(Integer id) {
        return (Calendar) calendarRepository.findById(id).orElse(new CalendarImpl());
    }

    @Override
    public void saveCalendars(Calendar calendar) {
        calendarRepository.save(calendar);
        calendarTableList.add(calendarRowTableRepository.findById(calendar.getId()));
    }

    @Override
    public void deleteCalendar(Calendar calendar) {
        calendarRepository.deleteById(calendar.getId());
    }

    @Override
    public void fillCalendars(TermCalculationData termCalculationData) {

        var calendarIds = termCalculationData.getProjectTasks().stream()
                .map(ProjectTask::getCalendarId).toList();

        List<Calendar> calendars = calendarRepository.findAllById(calendarIds);

        termCalculationData.setDefaultCalendar(defaultCalendar);
        termCalculationData.setCalendars(calendars);

    }

    public Calendar saveCalendar(Calendar calendar) {
        return calendarRepository.save(calendar);
    }

    public void deleteCalendars(List<Integer> ids) {
        calendarRepository.deleteAllById(ids);
    }

}
