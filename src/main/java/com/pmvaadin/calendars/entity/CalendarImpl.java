package com.pmvaadin.calendars.entity;

import com.pmvaadin.calendars.dayofweeksettings.DayOfWeekSettings;
import com.pmvaadin.calendars.exceptiondays.ExceptionDays;
import com.pmvaadin.calendars.OperationListenerForCalendar;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@EntityListeners(OperationListenerForCalendar.class)
@Getter
@NoArgsConstructor
@Table(name = "calendars")
@Transactional
public class CalendarImpl implements Calendar, Serializable, CalendarRowTable {
    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @OneToMany(mappedBy = "calendar",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ExceptionDays> calendarException;

    @Setter
    private String name;

    @Setter
    @Enumerated(EnumType.STRING)
    private CalendarSettings setting = CalendarSettings.EIGHTHOURWORKINGDAY;

    @Setter
    @Transient
    private String settingString;

    @Setter
    @OneToMany(mappedBy = "calendar",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})//, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayOfWeek ASC")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<DayOfWeekSettings> daysOfWeekSettings;

    public CalendarImpl(String name) {
        this.name = name;
    }

    public static String getHeaderName() {
        return "Name";
    }

    public static String getSettingName() {
        return "Setting";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof CalendarImpl that)) {
            return false;
        }

        return getId().equals(that.getId());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Date calculate(Date date, BigDecimal duration) {

        if (duration == null || new BigDecimal("0").equals(duration)) return date;

        List<DayOfWeekSettings> daysOfWeekSettings;
        if (setting == CalendarSettings.DAYSOFWEEKSETTINGS) daysOfWeekSettings = this.daysOfWeekSettings;
        else daysOfWeekSettings = this.setting.getDaysOfWeekSettings();



        return date;

    }

}

