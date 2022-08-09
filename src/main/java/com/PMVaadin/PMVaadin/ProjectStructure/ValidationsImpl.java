package com.PMVaadin.PMVaadin.ProjectStructure;

import java.util.List;

public class ValidationsImpl implements Validations {

    @Override
    public void checkQuantitiesTreeItemInTree(TreeItem<?> rootItem, List<? extends TreeItem<?>> treeItems) {

        int quantityInRootItem = getQuantityInRootItemRecursively(rootItem);

        if (quantityInRootItem != treeItems.size())
            throw new StandardError("Quantity of elements in rootTree and treeItems aren't equals");

    }

    private int getQuantityInRootItemRecursively(TreeItem<?> rootItem) {

        int quantity = 0;
        for (TreeItem<?> treeItem: rootItem.getChildren()) {
            quantity++;
            quantity = quantity + getQuantityInRootItemRecursively(treeItem);
        }

        return quantity;

    }

}
