package com.pmvaadin.projecttasks.entity;

import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.Date;
import java.util.Set;

public interface LinkedProjectTask extends HierarchyElement<Integer>{

    Date getStartDate();
    void setStartDate(Date startDate);
    Date getFinishDate();
    void setFinishDate(Date finishDate);

    //Set<Link> getLinks();

}
