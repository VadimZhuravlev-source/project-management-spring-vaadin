package com.PMVaadin.PMVaadin.ProjectStructure;

import com.PMVaadin.PMVaadin.Entities.ProjectTaskOrderedHierarchy;

import java.util.List;

public class TreeProjectTasksImpl<V extends ProjectTaskOrderedHierarchy> implements TreeProjectTasks<V>{

    @Override
    public void fillWbs(SimpleTree<V> rootItemSimpleTree) {

        fillWbsRecursively(rootItemSimpleTree.getChildren(), "");

    }

    private void fillWbsRecursively(List<SimpleTree<V>> children, String previousWbs) {

        for (SimpleTree<V> child: children) {

            V hierarchyElement = child.getValue();
            String newWbs = previousWbs + hierarchyElement.getLevelOrder().toString();
            hierarchyElement.setWbs(newWbs);
            fillWbsRecursively(child.getChildren(), newWbs + ".");

        }

    }

}
