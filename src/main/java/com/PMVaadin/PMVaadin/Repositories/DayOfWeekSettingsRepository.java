package com.PMVaadin.PMVaadin.Repositories;

import com.PMVaadin.PMVaadin.Entities.Calendar.DayOfWeekSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayOfWeekSettingsRepository extends JpaRepository<DayOfWeekSettings, Integer> {
    List<DayOfWeekSettings> findByCalendarId(Integer calendarId);
}
