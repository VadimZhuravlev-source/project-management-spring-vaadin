package com.pmvaadin.projecttasks.entity;

public interface ProjectTaskOrderedHierarchy extends HierarchyElement<Integer>{

    Integer getLevelOrder();
    void setLevelOrder(Integer levelOrder);
    String getWbs();
    void setWbs(String wbs);

}
