package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTask;

import java.util.List;

public interface Validations extends Unloopable {

    void checkQuantitiesTreeItemInTree(TreeItem rootItem, List<? extends TreeItem> treeItems) throws Exception;

}
