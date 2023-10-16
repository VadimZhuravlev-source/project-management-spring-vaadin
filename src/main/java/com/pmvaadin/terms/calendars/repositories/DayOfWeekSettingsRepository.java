package com.pmvaadin.terms.calendars.repositories;

import com.pmvaadin.terms.calendars.dayofweeksettings.DayOfWeekSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayOfWeekSettingsRepository extends JpaRepository<DayOfWeekSettings, Integer> {
    List<DayOfWeekSettings> findByCalendarId(Integer calendarId);
}
