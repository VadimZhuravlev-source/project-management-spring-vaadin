package com.PMVaadin.PMVaadin.Entities.ProjectTask;

public interface HierarchyElement<T> {

    T getId();
    void setId(T id);
    T getParentId();
    void setParentId(T parentId);
    T getNullId();

}
