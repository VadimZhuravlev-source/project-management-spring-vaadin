package com.pmvaadin.projecttasks.entity;

import com.pmvaadin.projecttasks.links.entities.Link;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

public interface LinkedProjectTask extends HierarchyElement<Integer>{

    LocalDateTime getStartDate();
    void setStartDate(LocalDateTime startDate);
    LocalDateTime getFinishDate();
    void setFinishDate(LocalDateTime finishDate);

    //Set<Link> getLinks();

}
