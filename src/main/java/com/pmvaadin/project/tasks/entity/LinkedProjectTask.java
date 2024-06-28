package com.pmvaadin.project.tasks.entity;

import java.time.LocalDateTime;

public interface LinkedProjectTask extends HierarchyElement<Integer>{

    LocalDateTime getStartDate();
    void setStartDate(LocalDateTime startDate);
    LocalDateTime getFinishDate();
    void setFinishDate(LocalDateTime finishDate);

    //Set<Link> getLinks();

}
