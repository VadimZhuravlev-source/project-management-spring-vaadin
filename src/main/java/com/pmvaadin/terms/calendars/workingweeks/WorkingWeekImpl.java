package com.pmvaadin.terms.calendars.workingweeks;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import com.pmvaadin.terms.calendars.entity.Calendar;
import com.pmvaadin.terms.calendars.entity.CalendarImpl;
import com.pmvaadin.terms.calendars.common.ExceptionLength;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
    @OneToMany(mappedBy = "workingWeek", //cascade = {CascadeType.PERSIST, CascadeType.MERGE})
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("dayOfWeek ASC")
    private List<WorkingTimeImpl> workingTimes = new ArrayList<>();

    public static WorkingWeekImpl getDefaultInstance(Calendar calendar) {
        var defaultWeek = new WorkingWeekImpl();
        defaultWeek.name = "Default";
        defaultWeek.isDefault = true;
        defaultWeek.calendar = (CalendarImpl) calendar;
        fillWorkingTimes(defaultWeek);
        return defaultWeek;
    }

    @Override
    public void fillDefaultWorkingTimes() {
        this.workingTimes.clear();
        fillWorkingTimes(this);
    }

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
    public WorkingWeek getInstance(Calendar calendar) {
        var workingWeek = new WorkingWeekImpl();
        workingWeek.calendar = (CalendarImpl) calendar;
        fillWorkingTimes(workingWeek);
        return workingWeek;
    }

    @Override
    public WorkingTime getWorkingTimeInstance() {
        var time = new WorkingTimeImpl();
        time.setWorkingWeek(this);
        return time;
    }

    @Override
    public Map<LocalDate, ExceptionLength> getExceptionAsDayConstraint() {

        if (this.start == null
                || this.finish == null
                || this.workingTimes == null
                || this.workingTimes.isEmpty())
            return new HashMap<>(0);

        var capacity = (int) ChronoUnit.DAYS.between(this.start, this.finish);
        capacity++;
        Map<LocalDate, ExceptionLength> map = new HashMap<>(capacity);

        var startPoint = this.start;
        var mapExceptionLength = new HashMap<DayOfWeek, ExceptionLength>(7);

        workingTimes.forEach(workingTime -> mapExceptionLength.put(workingTime.getDayOfWeek(), workingTime.getExceptionLength()));

        while (startPoint.compareTo(this.finish) <= 0) {
            var dayOfWeek= startPoint.getDayOfWeek();
            var exception = mapExceptionLength.get(dayOfWeek);
            if (exception != null)
                map.put(startPoint, exception);
            startPoint = startPoint.plusDays(1);
        }

        return map;

    }

    private static void fillWorkingTimes(WorkingWeekImpl workingWeek) {
        var workingTimes = Arrays.stream(DayOfWeek.values()).map(dayOfWeek -> {
            var workingTimeInstance = new WorkingTimeImpl();
            workingTimeInstance.setDayOfWeek(dayOfWeek);
            workingTimeInstance.setWorkingWeek(workingWeek);
            workingTimeInstance.fillIntervalsByDefault();
            return workingTimeInstance;
        }).collect(Collectors.toList());
        workingWeek.setWorkingTimes(workingTimes);
    }

}
