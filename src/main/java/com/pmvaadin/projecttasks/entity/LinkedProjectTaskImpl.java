package com.pmvaadin.projecttasks.entity;

import com.pmvaadin.projecttasks.links.entities.Link;

import java.util.HashSet;
import java.util.Set;

public class LinkedProjectTaskImpl extends ProjectTaskImpl implements LinkedProjectTask {

    private Set<Link> links = new HashSet<>();

    public Set<Link> getLinks() {
        return links;
    }

}
