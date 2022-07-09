package com.PMVaadin.PMVaadin.Services;

import com.PMVaadin.PMVaadin.Entities.Calendar.Calendar;
import com.PMVaadin.PMVaadin.Entities.Calendar.CalendarRowTable;
import com.PMVaadin.PMVaadin.Entities.Calendar.CalendarSettings;
import com.PMVaadin.PMVaadin.Repositories.CalendarRepository;
import com.PMVaadin.PMVaadin.Repositories.CalendarRowTableRepository;
import com.PMVaadin.PMVaadin.Repositories.DayOfWeekSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    private CalendarRepository calendarRepository;
    private CalendarRowTableRepository calendarRowTableRepository;
    private DayOfWeekSettingsRepository dayOfWeekSettingsRepository;

    @Autowired
    public void setCalendarRepository(CalendarRepository calendarRepository){
        this.calendarRepository = calendarRepository;
    }

    @Autowired
    public void setCalendarRowTableRepository(CalendarRowTableRepository calendarRowTableRepository){
        this.calendarRowTableRepository = calendarRowTableRepository;
    }

    @Autowired
    public void setDayOfWeekSettingsRepository(DayOfWeekSettingsRepository dayOfWeekSettingsRepository){
        this.dayOfWeekSettingsRepository = dayOfWeekSettingsRepository;
    }

    @Override
    public List<CalendarRowTable> getCalendars() {
        //return calendarRepository.findAll();
        List<CalendarRowTable> calendars = new ArrayList<>();
        Calendar calendar = new Calendar("New calendar");
        calendar.setSetting(CalendarSettings.DAYSOFWEEKSETTINGS);
        calendars.add(calendar);
        return calendars;
    }

    public Calendar getCalendar(Integer id) {
        Calendar foundedCalendar = calendarRepository.findById(id).get();
        return foundedCalendar;
    }

    public Calendar saveCalendar(Calendar calendar) {

        calendar.getDaysOfWeekSettings().forEach(dayOfWeekSettings -> dayOfWeekSettings.setCalendar(calendar));
        return calendarRepository.save(calendar);

    }

    public void deleteCalendars(List<Integer> ids) {

        calendarRepository.deleteAllById(ids);

    }

}
