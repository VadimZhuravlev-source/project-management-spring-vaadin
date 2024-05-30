package com.pmvaadin.security.user.labor.resource;

import com.pmvaadin.resources.labor.entity.LaborResourceRepresentation;
import com.pmvaadin.security.entities.UserImpl;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_labor_resources")
@Getter
public class UserLaborResourceImpl implements UserLaborResource {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserImpl user;

    @Setter
    @Column(name = "labor_resource_id")
    private Integer laborResourceId;

    @Setter
    private int sort;

    @Setter
    @Transient
    private LaborResourceRepresentation laborResource;

}
