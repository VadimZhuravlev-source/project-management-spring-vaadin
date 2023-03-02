package com.pmvaadin.projectstructure;

import com.pmvaadin.commonobjects.tree.TreeItem;

import java.util.*;
import java.util.stream.Collectors;

public class UnloopableImpl implements Unloopable{

    @Override
    public <T> Set<T> detectCycle(List<? extends TreeItem<T>> treeItems) {

        Map<TreeItem<T>, Object> cycledItems = new IdentityHashMap<>();

        // Floyd’s cycle detection algorithm
        for (TreeItem<T> treeItem : treeItems) {
            if (cycledItems.containsKey(treeItem)) continue;
            TreeItem<T> fastItem = treeItem;
            TreeItem<T> slowItem = treeItem;
            while (fastItem != null && fastItem.getParent() != null) {
                // move slow by one
                slowItem = slowItem.getParent();
                // move fast by two
                fastItem = fastItem.getParent().getParent();
                if (slowItem == fastItem) {
                    cycledItems.put(slowItem, null);
                    fastItem = slowItem.getParent();
                    while (slowItem != fastItem) {
                        cycledItems.put(fastItem, null);
                        fastItem = fastItem.getParent();
                    }
                    break;
                }

            }
        }

        if (cycledItems.isEmpty()) return new HashSet<>(0);

        return cycledItems.keySet().stream().map(TreeItem::getValue).collect(Collectors.toSet());

    }

}
