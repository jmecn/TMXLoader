package io.github.jmecn.tiled.loader;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.core.TiledMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.jmecn.tiled.TiledConst.*;

/**
 * @author yanmaoyuan
 */
public class LayerLoaders {

    static Logger logger = LoggerFactory.getLogger(LayerLoaders.class);

    private final AssetManager assetManager;

    private final AssetKey<?> assetKey;

    private final TiledMap map;

    LayerLoaders(AssetManager assetManager, AssetKey<?> assetKey, TiledMap map) {
        this.assetManager = assetManager;
        this.assetKey = assetKey;
        this.map = map;
    }

    public LayerLoader create(String layerType) {
        switch (layerType) {
            case LAYER:
                return new TileLayerLoader(assetManager, assetKey, map);
            case OBJECTGROUP:
                return new ObjectLayerLoader(assetManager, assetKey, map);
            case IMAGELAYER:
                return new ImageLayerLoader(assetManager, assetKey, map);
            case GROUP:
                return new GroupLayerLoader(assetManager, assetKey, map);
            default:
                if (!TILESET.equals(layerType) && !PROPERTIES.equals(layerType) && !TEXT_EMPTY.equals(layerType)) {
                    logger.warn("Unsupported layer type: {}", layerType);
                }
                return null;
        }
    }

}
