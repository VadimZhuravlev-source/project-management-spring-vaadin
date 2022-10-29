package com.PMVaadin.PMVaadin.ProjectTasks.Links.Entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "links")
public class LinkImpl implements Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Column(name = "id")
    private Integer id;

    // service fields
    @Version
    @Column(name = "version")
    private Integer version;

    @Setter
    @Column(name = "row_order", nullable = false)
    private Integer order;

    // main fields
    @Setter
    @Column(name = "project_task")
    private Integer projectTaskId;

//    @Setter
//    @ManyToOne
//    @JoinColumn(name = "project_task")
//    private ProjectTask projectTask;

    @Setter
    @Column(name = "linked_project_task")
    private Integer linkedProjectTaskId;

//    @Setter
//    @ManyToOne
//    @JoinColumn(name = "linked_project_task")
//    private ProjectTask linkedProjectTask;

    @Setter
    @Column(name = "link_type")
    private LinkType linkType;

}
