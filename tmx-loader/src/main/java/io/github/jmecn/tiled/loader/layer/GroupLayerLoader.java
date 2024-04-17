package io.github.jmecn.tiled.loader.layer;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.loader.LayerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.io.IOException;

import static io.github.jmecn.tiled.TiledConst.*;
import static io.github.jmecn.tiled.TiledConst.TEXT_EMPTY;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class GroupLayerLoader extends LayerLoader {

    private static final Logger logger = LoggerFactory.getLogger(GroupLayerLoader.class);
    private final TiledMap map;

    private ObjectLayerLoader objectLayerReader;
    private ImageLayerLoader imageLayerReader;
    private TileLayerLoader tileLayerReader;

    public GroupLayerLoader(AssetManager assetManager, AssetKey<?> key, TiledMap map) {
        super(assetManager, key);
        this.map = map;
    }

    private TileLayerLoader getTileLayerReader() {
        if (tileLayerReader == null) {
            tileLayerReader = new TileLayerLoader(assetManager, assetKey, map);
        }
        return tileLayerReader;
    }
    private ImageLayerLoader getImageLayerReader() {
        if (imageLayerReader == null) {
            imageLayerReader = new ImageLayerLoader(assetManager, assetKey, map);
        }
        return imageLayerReader;
    }

    private ObjectLayerLoader getObjectLayerReader() {
        if (objectLayerReader == null) {
            objectLayerReader = new ObjectLayerLoader(assetManager, assetKey, map);
        }
        return objectLayerReader;
    }

    @Override
    public GroupLayer load(Node node) throws IOException {
        GroupLayer groupLayer = new GroupLayer();
        readLayerBase(node, groupLayer);
        groupLayer.setMap(map);

        LayerLoaders layerLoaders = new LayerLoaders(assetManager, assetKey, map);

        Node child = node.getFirstChild();
        while (child != null) {
            // ignore properties
            if (!PROPERTIES.equals(child.getNodeName()) && !TEXT_EMPTY.equals(child.getNodeName())) {
                LayerLoader layerLoader = layerLoaders.create(child.getNodeName());
                if (layerLoader != null) {
                    Layer layer = layerLoader.load(child);
                    groupLayer.addLayer(layer);
                }
            }
            child = child.getNextSibling();
        }
        return groupLayer;
    }
}
