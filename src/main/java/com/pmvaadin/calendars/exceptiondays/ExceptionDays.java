package com.pmvaadin.calendars.exceptiondays;

import com.pmvaadin.calendars.entity.CalendarImpl;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "calendar_exception")
@Transactional
public class ExceptionDays {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalendarImpl calendar;

    @Column(name = "calendar_date")
    private LocalDate date;

    @Column(name = "duration")
    private int duration;

    @Transient
    private BigDecimal durationRepresentation;

    public static String getExceptionDaysName(){return "Date";}
}
