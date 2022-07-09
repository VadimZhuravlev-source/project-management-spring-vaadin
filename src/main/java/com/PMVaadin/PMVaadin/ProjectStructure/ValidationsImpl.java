package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.HierarchyElement;

import java.util.List;

public class ValidationsImpl<V extends HierarchyElement> implements Validations<V> {

    @Override
    public boolean detectCycle(List<SimpleTree<V>> treeItems) {

        // Floyd’s cycle detection algorithm
        for (SimpleTree<V> treeItem : treeItems) {
            SimpleTree<V> fastItem = treeItem;
            SimpleTree<V> slowItem = treeItem;
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
}
