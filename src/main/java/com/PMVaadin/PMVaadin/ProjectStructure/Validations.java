package com.PMVaadin.PMVaadin.ProjectStructure;

import java.util.List;

public interface Validations {

    void checkQuantitiesTreeItemInTree(TreeItem<?> rootItem, List<? extends TreeItem<?>> treeItems) throws Exception;

}
