package com.pmvaadin.projectstructure.termscalculation;

import com.pmvaadin.projecttasks.entity.LinkedProjectTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class LinkedProjectTaskDTO implements LinkedProjectTask {

    @Setter
    private Integer id;
    @Setter
    private Integer parentId;

    private Integer version;

    @Setter
    private Date startDate;

    @Setter
    private Date finishDate;

    public Integer getNullId() {
        return 0;
    }

}
