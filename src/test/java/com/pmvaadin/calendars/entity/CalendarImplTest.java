package com.pmvaadin.calendars.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CalendarImplTest {

    CalendarImpl calendar = new CalendarImpl();
    CalendarImpl calendar12;
    CalendarImpl calendar24;

    {
        calendar12 = new CalendarImpl();
        calendar12.setSetting(CalendarSettings.HOURSHIFT12);
        calendar24 = new CalendarImpl();
        calendar24.setSetting(CalendarSettings.HOURSHIFT24);
    }

    @Test
    void getDuration() {


    }

    @Test
    void getDateByDuration() {

        LocalDateTime date = LocalDateTime.of(2022, 1, 25, 10, 23, 11);

        LocalDateTime newDate = calendar.getDateByDuration(date, 3);
        LocalDateTime plusSecond = date.plusSeconds(3);
        assertEquals(plusSecond, newDate);

        newDate = calendar.getDateByDuration(date, -3);
        LocalDateTime minusSecond = date.minusSeconds(3);
        assertEquals(minusSecond, newDate);

    }
}