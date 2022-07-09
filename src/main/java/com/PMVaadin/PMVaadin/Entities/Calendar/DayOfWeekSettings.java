package com.PMVaadin.PMVaadin.Entities.Calendar;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

//    @Column(name = "calendar_id")
//    private UUID calendarId;

    @Setter
    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @Setter
    //@Enumerated(EnumType.STRING)
    //@Convert
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

    public void fillDayOfWeekString() {
        dayOfWeekString = DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof DayOfWeekSettings)) {
            return false;
        }

        DayOfWeekSettings that = (DayOfWeekSettings) o;

        return getId().equals(that.getId()) && getVersion().equals(that.getVersion());
    }

    @Override
    public String toString() {
        return dayOfWeek.toString() + " " + countHours.toString();
    }


}
