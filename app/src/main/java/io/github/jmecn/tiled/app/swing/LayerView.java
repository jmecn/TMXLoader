package io.github.jmecn.tiled.app.swing;

import io.github.jmecn.tiled.core.GroupLayer;
import io.github.jmecn.tiled.core.Layer;
import io.github.jmecn.tiled.core.TiledMap;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class LayerView extends JTree {
    private transient TiledMap tiledMap;

    public LayerView() {
        this.tiledMap = null;
        this.setPreferredSize(new Dimension(200, 0));
        this.setRootVisible(false);
        this.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Layers")));
    }

    public void setTiledMap(TiledMap map) {
        this.tiledMap = map;
        update();
    }

    public void update() {
        if (tiledMap == null) {
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Layers");
        for (int i = 0; i < tiledMap.getLayerCount(); i++) {
            Layer layer = tiledMap.getLayer(i);
            addLayer(root, layer);
        }

        setModel(new DefaultTreeModel(root));
    }

    private void addLayer(DefaultMutableTreeNode node, Layer layer) {
        DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(layer.getName());
        node.add(subNode);
        if (layer instanceof GroupLayer) {
            GroupLayer group = (GroupLayer) layer;
            for (int j = 0; j < group.getLayerCount(); j++) {
                Layer subLayer = group.getLayer(j);
                addLayer(subNode, subLayer);
            }
        }
    }
}
