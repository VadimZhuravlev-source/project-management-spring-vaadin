package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;

import java.util.List;

public class ValidationsImpl<V extends HierarchyElement> implements Validations<V> {

    @Override
    public boolean detectCycle(List<TreeItem<V>> treeItems) {

        // Floyd’s cycle detection algorithm
        for (TreeItem<V> treeItem : treeItems) {
            TreeItem<V> fastItem = treeItem;
            TreeItem<V> slowItem = treeItem;
            while (fastItem != null && fastItem.getParent() != null) {
                // move slow by one
                slowItem = slowItem.getParent();
                // move fast by two
                fastItem = fastItem.getParent().getParent();
                if (slowItem == fastItem) return true;
            }
        }

        return false;

    }

    @Override
    public boolean checkQuantitiesTreeItemInTree(TreeItem<V> rootItem, List<TreeItem<V>> treeItems) {

        int quantityInRootItem = getQuantityInRootItemRecursively(rootItem);

        return quantityInRootItem == treeItems.size();

    };

    private int getQuantityInRootItemRecursively(TreeItem<V> rootItem) {

        int quantity = 0;
        for (TreeItem<V> treeItem: rootItem.getChildren()) {
            quantity++;
            quantity = quantity + getQuantityInRootItemRecursively(treeItem);
        }

        return quantity;

    }

}
