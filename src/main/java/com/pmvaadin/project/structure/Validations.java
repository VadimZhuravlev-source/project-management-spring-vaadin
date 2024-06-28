package com.pmvaadin.project.structure;

import com.pmvaadin.common.tree.TreeItem;

import java.util.List;

public interface Validations {

    void checkQuantitiesTreeItemInTree(TreeItem<?> rootItem, List<? extends TreeItem<?>> treeItems);

}
