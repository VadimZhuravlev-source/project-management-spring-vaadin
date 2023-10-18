package com.pmvaadin.terms.calculation;

import com.pmvaadin.projecttasks.entity.LinkedProjectTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@Getter
public class LinkedProjectTaskDTO {//implements LinkedProjectTask {

    @Setter
    private Integer id;
    @Setter
    private Integer parentId;

    private Integer version;

    @Setter
    private String name;

//    @Setter
//    private LocalDateTime startDate;
//
//    @Setter
//    private LocalDateTime finishDate;

    public Integer getNullId() {
        return 0;
    }

}
