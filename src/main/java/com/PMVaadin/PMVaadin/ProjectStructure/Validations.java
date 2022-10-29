package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.CommonObjects.Tree.TreeItem;

import java.util.List;

public interface Validations {

    void checkQuantitiesTreeItemInTree(TreeItem<?> rootItem, List<? extends TreeItem<?>> treeItems);

}
