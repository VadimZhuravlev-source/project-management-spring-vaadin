package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private String name = "";

    @Setter
    private LocalDate start;

    @Setter
    private LocalDate finish;

    @Setter
    private int sort = 0;

    @Setter
    @Column(name = "is_default")
    private boolean isDefault = false;

    @Setter
    @OneToMany(mappedBy = "workingWeek")
    @OrderBy("dayOfWeek ASC")
    private List<WorkingTimeImpl> workingTimes = new ArrayList<>();

    @Override
    public void nullIdentifyingFields() {

        this.id = null;
        this.version = null;

        if (workingTimes == null) return;

        workingTimes.forEach(WorkingTimeImpl::nullIdentifyingFields);

    }

    @Override
    public List<WorkingTime> getWorkingTimes() {
        return workingTimes.stream().map(w -> (WorkingTime) w).collect(Collectors.toList());
    }

    @Override
    public WorkingWeek getInstance() {
        return new WorkingWeekImpl();
    }

    @Override
    public WorkingTime getWorkingTimeInstance() {
        var time = new WorkingTimeImpl();
        time.setWorkingWeek(this);
        return time;
    }

}
