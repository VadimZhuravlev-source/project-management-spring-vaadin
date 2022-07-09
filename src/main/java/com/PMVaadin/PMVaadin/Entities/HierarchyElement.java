package com.PMVaadin.PMVaadin.Entities;

public interface HierarchyElement<T> {

    T getId();
    void setId(T id);
    T getParentId();
    void setParentId(T parentId);

}
