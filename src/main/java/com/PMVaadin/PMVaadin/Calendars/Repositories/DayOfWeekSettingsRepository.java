package com.PMVaadin.PMVaadin.Calendars.Repositories;

import com.PMVaadin.PMVaadin.Calendars.DayOfWeekSettings.DayOfWeekSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayOfWeekSettingsRepository extends JpaRepository<DayOfWeekSettings, Integer> {
    List<DayOfWeekSettings> findByCalendarId(Integer calendarId);
}
