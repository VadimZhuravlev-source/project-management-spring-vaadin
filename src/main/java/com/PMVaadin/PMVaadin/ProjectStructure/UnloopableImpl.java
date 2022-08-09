package com.PMVaadin.PMVaadin.ProjectStructure;

import java.util.List;

public class UnloopableImpl implements Unloopable{

    @Override
    public void detectCycle(List<? extends TreeItem<?>> treeItems) {

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
                    throw new StandardError("Detect cycle in tree with element: " + treeItem.getValue().toString());
            }
        }

    }

}
