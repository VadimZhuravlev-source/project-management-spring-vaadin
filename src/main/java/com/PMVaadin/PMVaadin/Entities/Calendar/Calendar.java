package com.PMVaadin.PMVaadin.Entities.Calendar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@EntityListeners(OperationListenerForCalendar.class)
@Getter
@NoArgsConstructor
@Table(name = "calendars")
public class Calendar implements Serializable, CalendarRowTable {

    @Id
    @Setter
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Transient
    @Setter
    private Exception exception;

    @Setter
    private String name = "";

    @Setter
    @Enumerated(EnumType.STRING)
//    @ManyToOne
//    @JoinColumn(name = "id")
    private CalendarSettings setting;

    @Setter
    @Transient
    private String settingString;

//    @OneToMany
//    @JoinColumn(name = "calendar_id", referencedColumnName = "id")
    @Setter
    @OneToMany(mappedBy = "calendar", fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})//, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayOfWeek ASC")
    private List<DayOfWeekSettings> daysOfWeekSettings;

//    private List<ExceptionDay> exceptionDaysSettings;

    public Calendar(Exception exception){
        this.exception = exception;
    }

    public Calendar(String name){
        this.name = name;
    }

    public static Calendar getNewCalendar() {

        Calendar newCalendar = new Calendar();
        newCalendar.setting = CalendarSettings.EIGHTHOURWORKINGDAY;
        newCalendar.settingString = newCalendar.setting.toString();
        newCalendar.daysOfWeekSettings = newCalendar.setting.getDaysOfWeekSettings();

        return newCalendar;

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

        if (!(o instanceof Calendar)) {
            return false;
        }

        Calendar that = (Calendar) o;

        return getId().equals(that.getId()) && getVersion().equals(that.getVersion()) ;
    }

    @Override
    public String toString() {
        return name;
    }

}

