package com.PMVaadin.PMVaadin.Entities.calendar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "calendar_exception")
@Transactional
public class ExceptionDays {

    @Id
    @Setter
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalendarImpl calendar;

    @Setter
    @Column(name = "calendar_date")
    private LocalDate date;

    public static String getExceptionDaysName(){return "Date";}
}
