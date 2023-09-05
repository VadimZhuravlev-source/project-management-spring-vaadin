package com.pmvaadin.projecttasks.links.entities;

import com.pmvaadin.projecttasks.entity.ProjectTask;
import com.pmvaadin.terms.timeunit.entity.TimeUnit;
import com.pmvaadin.terms.timeunit.entity.TimeUnitImpl;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Entity
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
    private Integer sort;

    // main fields
    @Setter
    @Column(name = "project_task")
    private Integer projectTaskId;

    @Setter
    @Column(name = "linked_project_task")
    private Integer linkedProjectTaskId;

    @Setter
    @Column(name = "link_type")
    private LinkType linkType;

    @Setter
    @Column(name = "lag")
    private long lag = 0L;

    @Setter
    @Transient
    private BigDecimal lagRepresentation;

    @Setter
    @Transient
    private ProjectTask linkedProjectTask;

    @Setter
    @Transient
    private String representation;

    @ManyToOne
    @JoinColumn(name = "time_unit_id", nullable = false)
    private TimeUnitImpl timeUnit;

    public LinkImpl() {
        linkType = LinkType.FINISHSTART;
    }

    public LinkImpl(Link link) {
        this.projectTaskId = link.getProjectTaskId();
        this.linkType = link.getLinkType();
    }

    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = (TimeUnitImpl) timeUnit;
    };

    @Override
    public Link getInstance() {
        return new LinkImpl();
    }

    @Override
    public Link copy(Link link) {
        return new LinkImpl(link);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkImpl that)) return false;
        if (getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) return super.hashCode();
        return Objects.hash(getId());
    }

}
