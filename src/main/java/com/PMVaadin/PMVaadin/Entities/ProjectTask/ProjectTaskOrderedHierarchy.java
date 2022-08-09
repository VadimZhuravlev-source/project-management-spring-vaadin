package com.PMVaadin.PMVaadin.Entities.ProjectTask;

public interface ProjectTaskOrderedHierarchy extends HierarchyElement<Integer>{

    Integer getLevelOrder();
    void setLevelOrder(Integer levelOrder);
    String getWbs();
    void setWbs(String wbs);

}
