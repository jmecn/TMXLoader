package io.github.jmecn.tiled.loader;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.core.ImageLayer;
import io.github.jmecn.tiled.core.TiledImage;
import io.github.jmecn.tiled.core.TiledMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static io.github.jmecn.tiled.TiledConst.*;
import static io.github.jmecn.tiled.loader.Utils.getAttribute;

/**
 * Tiled Image Layer Loader.
 *
 * @author yanmaoyuan
 */
public class ImageLayerLoader extends AbstractLayerLoader {
    private static final Logger logger = LoggerFactory.getLogger(ImageLayerLoader.class);
    private TiledMap map;

    public ImageLayerLoader(AssetManager assetManager, AssetKey<?> key, TiledMap map) {
        super(assetManager, key);
        this.map = map;
    }


    /**
     * read ImageLayer
     *
     * @param node the node representing the "imagelayer" element
     * @return the loaded image layer
     */
    public ImageLayer load(Node node) {
        int width = getAttribute(node, WIDTH, map.getWidth());
        int height = getAttribute(node, HEIGHT, map.getHeight());
        boolean repeatX = getAttribute(node, "repeatx", 0) == 1;
        boolean repeatY = getAttribute(node, "repeaty", 0) == 1;

        ImageLayer layer = new ImageLayer(width, height);
        readLayerBase(node, layer);
        layer.setRepeatX(repeatX);
        layer.setRepeatY(repeatY);

        boolean hasImage = false;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (IMAGE.equals(nodeName)) {
                TiledImage image = tiledImageLoader.load(child);
                if (image.getTexture() != null) {
                    layer.setSource(image.getSource());
                    layer.setTexture(image.getTexture());
                    layer.setMaterial(image.getMaterial());

                    hasImage = true;
                    break;
                }
            }
        }

        if (!hasImage) {
            logger.warn("ImageLayer {} has no image", layer.getName());
            throw new IllegalArgumentException("ImageLayer " + layer.getName() + " has no image");
        }

        return layer;
    }
}
