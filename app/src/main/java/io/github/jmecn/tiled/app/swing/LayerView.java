package io.github.jmecn.tiled.app.swing;

import io.github.jmecn.tiled.core.*;

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
        this.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
    }

    public void setTiledMap(TiledMap map) {
        this.tiledMap = map;
        update();
    }

    public void update() {
        if (tiledMap == null) {
            setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        int count = tiledMap.getLayerCount();
        for (int i = 0; i < tiledMap.getLayerCount(); i++) {
            Layer layer = tiledMap.getLayer(count - i - 1);
            addLayer(root, layer);
        }

        setModel(new DefaultTreeModel(root));
    }

    private void addLayer(DefaultMutableTreeNode node, Layer layer) {
        DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(layer);
        node.add(subNode);
        if (layer instanceof GroupLayer) {
            GroupLayer group = (GroupLayer) layer;
            int count = group.getLayerCount();
            for (int j = 0; j < group.getLayerCount(); j++) {
                Layer subLayer = group.getLayer(count - j - 1);
                addLayer(subNode, subLayer);
            }
        }
    }

}
