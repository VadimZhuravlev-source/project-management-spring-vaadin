package com.pmvaadin.projecttasks.entity;

public interface HierarchyElement<T> {

    T getId();
    void setId(T id);
    T getParentId();
    void setParentId(T parentId);
    T getNullId();

}
