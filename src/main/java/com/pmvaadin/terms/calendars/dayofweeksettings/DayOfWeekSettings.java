package com.pmvaadin.terms.calendars.dayofweeksettings;

import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

@Entity
@EntityListeners(OperationListenerForDayOfWeekSettings.class)
@NoArgsConstructor
@Getter
@Table(name = "day_of_week_settings")
public class DayOfWeekSettings implements Serializable {

    @Setter
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalendarImpl calendar;

    @Setter
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Setter
    @Transient
    private String dayOfWeekString;

    @Setter
    @Column(name = "duration")
    private int countHours;

    public DayOfWeekSettings(Integer dayOfWeek, int countHours) {

        this.dayOfWeek = dayOfWeek;
        fillDayOfWeekString();
        this.countHours = countHours;

    }

    public DayOfWeekSettings(DefaultDaySetting dayOfWeekSettings) {
        this.dayOfWeek = dayOfWeekSettings.dayOfWeek();
        this.countHours = dayOfWeekSettings.countSeconds();
    }

    public static String getWorkDaysName() {
        return "Day of week";
    }

    public static String getHourOfWorkName() {
        return "Hours";
    }

    public void fillDayOfWeekString() {
        dayOfWeekString = DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkImpl that)) return false;
        if (getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) return super.hashCode();
        return Objects.hash(getId());
    }

}
