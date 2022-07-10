package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;

import java.util.List;

public interface Validations<V extends HierarchyElement> extends Unloopable<V> {

    boolean checkQuantitiesTreeItemInTree(TreeItem<V> rootItem, List<TreeItem<V>> treeItems);

}
