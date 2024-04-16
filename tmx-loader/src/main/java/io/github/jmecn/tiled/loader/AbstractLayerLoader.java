package io.github.jmecn.tiled.loader;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.core.Layer;
import io.github.jmecn.tiled.util.ColorUtil;
import org.w3c.dom.Node;

import java.util.Properties;

import static io.github.jmecn.tiled.TiledConst.CLASS;
import static io.github.jmecn.tiled.TiledConst.NAME;
import static io.github.jmecn.tiled.loader.Utils.*;
import static io.github.jmecn.tiled.loader.Utils.getDoubleAttribute;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public abstract class AbstractLayerLoader {

    protected AssetManager assetManager;
    protected AssetKey<?> assetKey;
    protected PropertyLoader propertiesLoader;
    protected TiledImageLoader tiledImageLoader;

    protected AbstractLayerLoader(AssetManager assetManager, AssetKey<?> key) {
        this.assetManager = assetManager;
        this.assetKey = key;

        this.propertiesLoader = new PropertyLoader(assetManager, key);
        this.tiledImageLoader = new TiledImageLoader(assetManager, key);
    }

    /**
     * read the common part of a Layer
     *
     * @param node the Layer node
     * @param layer the Layer
     */
    protected void readLayerBase(Node node, Layer layer) {
        String id = getAttributeValue(node, "id");
        if (id != null) {
            layer.setId(Integer.parseInt(id));
        }

        final String name = getAttributeValue(node, NAME);
        String clazz = getAttribute(node, CLASS, "");
        double opacity = getDoubleAttribute(node, "opacity", 1.0);
        boolean visible = getAttribute(node, "visible", 1) == 1;
        boolean locked = getAttribute(node, "locked", 0) == 1;
        String tintColor = getAttributeValue(node, "tintcolor");
        int offsetX = getAttribute(node, "offsetx", 0);
        int offsetY = getAttribute(node, "offsety", 0);
        float parallaxX = (float) getDoubleAttribute(node, "parallaxx", 1.0);
        float parallaxY = (float) getDoubleAttribute(node, "parallaxy", 1.0);

        layer.setName(name);
        layer.setClazz(clazz);
        layer.setOpacity(opacity);

        if (tintColor != null) {
            layer.setTintColor(ColorUtil.toColorRGBA(tintColor));
        }

        // This is done at the end, otherwise the offset is applied during
        // the loading of the tiles.
        layer.setOffset(offsetX, offsetY);

        // The parallax scrolling factor determines the amount by which the layer
        // moves in relation to the camera.
        layer.setParallaxFactor(parallaxX, parallaxY);

        // Invisible layers are automatically locked, so it is important to
        // set the layer to potentially invisible _after_ the layer data is
        // loaded.
        layer.setVisible(visible);

        layer.setLocked(locked);

        // read properties
        Properties props = propertiesLoader.load(node.getChildNodes());
        layer.setProperties(props);
    }
}
