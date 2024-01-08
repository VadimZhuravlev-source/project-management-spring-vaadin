package com.pmvaadin.resources.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "labor_resources")
public class LaborResourceImpl implements LaborResource {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    @Setter
    private Integer version;

    @Setter
    private String name;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaborResourceImpl that)) return false;
        if (getId() == null || that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) return super.hashCode();
        return Objects.hash(getId());
    }

}
