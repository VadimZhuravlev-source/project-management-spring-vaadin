package com.PMVaadin.PMVaadin.Calendars.Services;

import com.PMVaadin.PMVaadin.Calendars.Entity.Calendar;
import com.PMVaadin.PMVaadin.Calendars.Entity.CalendarImpl;
import com.PMVaadin.PMVaadin.Calendars.Repositories.CalendarRepository;
import com.PMVaadin.PMVaadin.Calendars.Repositories.CalendarRowTableRepository;
import com.PMVaadin.PMVaadin.Calendars.Repositories.DayOfWeekSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

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


    public Calendar saveCalendar(Calendar calendar) {
        return calendarRepository.save(calendar);
    }

    public void deleteCalendars(List<Integer> ids) {
        calendarRepository.deleteAllById(ids);
    }

}
