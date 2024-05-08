package io.github.jmecn.tiled.app.swing;

import io.github.jmecn.tiled.core.ImageLayer;
import io.github.jmecn.tiled.core.ObjectGroup;
import io.github.jmecn.tiled.core.TileLayer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class LayerCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Configures the renderer based on the passed in components.
     * The value is set from messaging the tree with
     * <code>convertValueToText</code>, which ultimately invokes
     * <code>toString</code> on <code>value</code>.
     * The foreground color is set based on the selection and the icon
     * is set based on the <code>leaf</code> and <code>expanded</code>
     * parameters.
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object object = node.getUserObject();

        if (object instanceof TileLayer) {

        } else if (object instanceof ObjectGroup) {

        } else if (object instanceof ImageLayer) {

        }
        return this;
    }
}
