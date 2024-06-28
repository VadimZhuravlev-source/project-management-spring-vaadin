package com.pmvaadin.terms.calculation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
