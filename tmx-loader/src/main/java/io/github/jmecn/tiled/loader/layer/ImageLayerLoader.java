package io.github.jmecn.tiled.loader.layer;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.core.ImageLayer;
import io.github.jmecn.tiled.core.TiledImage;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.loader.LayerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import static io.github.jmecn.tiled.TiledConst.*;
import static io.github.jmecn.tiled.loader.Utils.getAttribute;
import static io.github.jmecn.tiled.loader.Utils.getChildByTag;

/**
 * Tiled Image Layer Loader.
 *
 * @author yanmaoyuan
 */
public class ImageLayerLoader extends LayerLoader {
    private static final Logger logger = LoggerFactory.getLogger(ImageLayerLoader.class);
    private final TiledMap map;

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
    @Override
    public ImageLayer load(Node node) {
        int width = getAttribute(node, WIDTH, map.getWidth());
        int height = getAttribute(node, HEIGHT, map.getHeight());
        boolean repeatX = getAttribute(node, REPEAT_X, 0) == 1;
        boolean repeatY = getAttribute(node, REPEAT_Y, 0) == 1;

        ImageLayer layer = new ImageLayer(width, height);
        readLayerBase(node, layer);
        layer.setRepeatX(repeatX);
        layer.setRepeatY(repeatY);

        Node imageNode = getChildByTag(node, IMAGE);
        if (imageNode == null) {
            logger.warn("ImageLayer {} has no image", layer.getName());
        } else {
            TiledImage image = imageLoader.load(imageNode);
            layer.setImage(image);
        }

        return layer;
    }
}
