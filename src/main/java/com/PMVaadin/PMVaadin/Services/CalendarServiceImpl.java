package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.Calendar.Calendar;
import com.PMVaadin.PMVaadin.Repositories.CalendarRepository;
import com.PMVaadin.PMVaadin.Repositories.CalendarRowTableRepository;
import com.PMVaadin.PMVaadin.Repositories.DayOfWeekSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public  Calendar getCalendarById(Integer id) {
       return calendarRepository.findById(id).orElse(new Calendar());
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
