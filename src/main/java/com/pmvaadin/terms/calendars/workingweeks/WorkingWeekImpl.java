package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "working_weeks")
@Getter
public class WorkingWeekImpl implements WorkingWeek, HasIdentifyingFields {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalendarImpl calendar;

    @Setter
    private String name;

    @Setter
    private LocalDate start;

    @Setter
    private LocalDate finish;

    @Setter
    private Integer sort;

    @Setter
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DayOfWeekSettingImpl> workingDaySettings = new ArrayList<>();

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }

}
