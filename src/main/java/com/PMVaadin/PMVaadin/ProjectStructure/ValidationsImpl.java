package com.PMVaadin.PMVaadin.ProjectStructure;

import java.util.List;

public class ValidationsImpl implements Validations {

    @Override
    public void detectCycle(List<? extends TreeItem<?>> treeItems) throws Exception {

        // Floyd’s cycle detection algorithm
        for (TreeItem<?> treeItem : treeItems) {
            TreeItem<?> fastItem = treeItem;
            TreeItem<?> slowItem = treeItem;
            while (fastItem != null && fastItem.getParent() != null) {
                // move slow by one
                slowItem = slowItem.getParent();
                // move fast by two
                fastItem = fastItem.getParent().getParent();
                if (slowItem == fastItem)
                    throw new Exception("Detect cycle in tree with element: " + treeItem.getValue().toString());
            }
        }

    }

    @Override
    public void checkQuantitiesTreeItemInTree(TreeItem<?> rootItem, List<? extends TreeItem<?>> treeItems) throws Exception {

        int quantityInRootItem = getQuantityInRootItemRecursively(rootItem);

        if (quantityInRootItem != treeItems.size())
            throw new Exception("Quantity of elements in rootTree and treeItems aren't equals");

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
