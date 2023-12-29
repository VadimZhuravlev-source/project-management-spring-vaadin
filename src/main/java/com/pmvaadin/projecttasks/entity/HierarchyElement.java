package com.pmvaadin.projecttasks.entity;

public interface HierarchyElement<T> {

    T getId();
    void setId(T id);
    Integer getVersion();
    T getParentId();
    void setParentId(T parentId);
    void setUniqueValueIfParentIdNull();
    void revertParentIdNull();
    T getNullId();

}
