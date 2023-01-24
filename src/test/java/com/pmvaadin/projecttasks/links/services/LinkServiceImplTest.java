package com.pmvaadin.projecttasks.links.services;

import com.pmvaadin.projecttasks.links.entities.Link;
import com.pmvaadin.projecttasks.links.entities.LinkImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceImplTest {

    @Test
    void validate() {

        Set<Link> linkSet = new HashSet<>();
        Link link1 = new LinkImpl();
        link1.setId(1);
        link1.setRepresentation("1");
        linkSet.add(link1);
        Link link2 = new LinkImpl();
        link1.setId(2);
        link2.setRepresentation("2");
        linkSet.add(link2);

        String.join("->", linkSet.stream().map(Link::getRepresentation).toList());

    }
}