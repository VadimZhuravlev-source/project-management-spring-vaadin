package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.calendar.Calendar;
import com.PMVaadin.PMVaadin.Entities.calendar.CalendarImpl;
import com.PMVaadin.PMVaadin.Repositories.CalendarRepository;
import com.PMVaadin.PMVaadin.Repositories.CalendarRowTableRepository;
import com.PMVaadin.PMVaadin.Repositories.DayOfWeekSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    private CalendarRepository calendarRepository;
    private CalendarRowTableRepository calendarRowTableRepository;
    private DayOfWeekSettingsRepository dayOfWeekSettingsRepository;
    private List<CalendarImpl> calendarTableList;

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
    public List<CalendarImpl> getCalendars() {
        return calendarRowTableRepository.findAll();
    }

    @Override
    public CalendarImpl getCalendarById(Integer id) {
        return (CalendarImpl) calendarRepository.findById(id).orElse(new CalendarImpl());
    }

    @Override
    public void saveCalendars(CalendarImpl calendar) {
        calendarRepository.save(calendar);
        calendarTableList.add(calendarRowTableRepository.findById(calendar.getId()));
    }

    @Override
    public void deleteCalendar(CalendarImpl calendar) {
        calendarRepository.deleteById(calendar.getId());
    }


    public CalendarImpl saveCalendar(CalendarImpl calendar) {
        return calendarRepository.save(calendar);
    }

    public void deleteCalendars(List<Integer> ids) {
        calendarRepository.deleteAllById(ids);
    }

}
