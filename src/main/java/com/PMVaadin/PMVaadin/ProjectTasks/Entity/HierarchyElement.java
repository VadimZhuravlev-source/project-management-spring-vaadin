package com.PMVaadin.PMVaadin.ProjectTasks.Entity;

public interface HierarchyElement<T> {

    T getId();
    void setId(T id);
    T getParentId();
    void setParentId(T parentId);
    T getNullId();

}
