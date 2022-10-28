package com.PMVaadin.PMVaadin.Entities.calendar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
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
    @Column(name = "count_hours")
    private BigDecimal countHours;

    public DayOfWeekSettings(Integer dayOfWeek, BigDecimal countHours) {

        this.dayOfWeek = dayOfWeek;
        fillDayOfWeekString();
        this.countHours = countHours;

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
    public int hashCode() {
        return Objects.hash(id, version);
    }

}
