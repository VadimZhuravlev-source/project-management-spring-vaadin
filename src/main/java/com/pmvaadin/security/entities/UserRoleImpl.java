package com.pmvaadin.security.entities;

import com.pmvaadin.terms.calendars.common.HasIdentifyingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_roles")
@Getter
public class UserRoleImpl implements UserRole, HasIdentifyingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Integer id;

    @Version
    private Integer version;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserImpl user;

    @Setter
    @Column(name = "role_id")
    private Role role;

    @Override
    public void nullIdentifyingFields() {
        this.id = null;
        this.version = null;
    }
}
