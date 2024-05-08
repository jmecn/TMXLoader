package io.github.jmecn.tiled.renderer;

import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.renderer.factory.SpriteFactory;
import io.github.jmecn.tiled.math2d.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>
 * we don't really draw 2d image in a 3d game engine, instead I create spatials
 * and apply Material to tiles and objects.
 * </p>
 *
 * In Tiled Qt they use XOY axis, X positive to right and Y positive to down
 *
 * <pre>
 * O------- X
 * |
 * |
 * |
 * Y
 * </pre>
 *
 * Once in jme3 I choose XOY plane, which means I have to modify the Y for every
 * tile and every object. Now I choose XOZ plane, it's much easier to do the
 * math.
 *
 * <p>The Point(x,y) in Tiled now converted to Vector3f(x, 0, y).</p>
 *
 * <pre>
 * O------- X
 * |
 * |
 * |
 * Z
 * </pre>
 *
 * @author yanmaoyuan
 */
public abstract class MapRenderer {

    static Logger logger = LoggerFactory.getLogger(MapRenderer.class);

    protected double layerDistance = 16f;// the distance between layers
    protected double layerGap = 1f;// the gap between layers
    protected double step;
    protected boolean isTintingColorEnabled = true;

    protected TiledMap tiledMap;
    protected int width;
    protected int height;
    protected int tileWidth;
    protected int tileHeight;

    protected Node rootNode;
    protected List<Layer> sortedLayers;
    protected Map<Layer, LayerNode> layerNodeMap;// save the layer-node relation
    protected SpriteFactory spriteFactory;

    /**
     * The whole map size in pixel
     */
    protected Point mapSize;

    protected MapRenderer(TiledMap tiledMap) {
        this.tiledMap = tiledMap;
        this.width = tiledMap.getWidth();
        this.height = tiledMap.getHeight();
        this.tileWidth = tiledMap.getTileWidth();
        this.tileHeight = tiledMap.getTileHeight();
        this.step = layerDistance / (height * width);
        this.mapSize = new Point(width * tileWidth, height * tileHeight);

        this.rootNode = new Node("TileMap");
        this.rootNode.setQueueBucket(RenderQueue.Bucket.Opaque);

        this.layerNodeMap = new HashMap<>();
        sortLayers();
    }

    public static MapRenderer create(TiledMap tiledMap) {
        switch (tiledMap.getOrientation()) {
            case ORTHOGONAL:
                return new OrthogonalRenderer(tiledMap);
            case ISOMETRIC:
                return new IsometricRenderer(tiledMap);
            case STAGGERED:
                return new StaggeredRenderer(tiledMap);
            case HEXAGONAL:
                return new HexagonalRenderer(tiledMap);
            default:
                throw new IllegalArgumentException("Unsupported orientation: " + tiledMap.getOrientation());
        }
    }

    public void sortLayers() {
        List<Layer> layers = new ArrayList<>();

        for (Layer layer : tiledMap.getLayers()) {
            if (layer instanceof GroupLayer) {
                sortLayers(layers, (GroupLayer) layer);
            } else {
                layers.add(layer);
            }
        }

        sortedLayers = Collections.unmodifiableList(layers);
    }

    public List<Layer> getSortedLayers() {
        return sortedLayers;
    }

    /**
     * Depth first search to sort layers
     * @param list the list to save sorted layers
     * @param group the group layer
     */
    private void sortLayers(List<Layer> list, GroupLayer group) {
        for (Layer layer : group.getLayers()) {
            if (layer instanceof GroupLayer) {
                sortLayers(list, (GroupLayer) layer);
            } else {
                list.add(layer);
            }
        }
    }

    /**
     * Set the sprite factory
     * @param spriteFactory the sprite factory
     */
    public void setSpriteFactory(SpriteFactory spriteFactory) {
        this.spriteFactory = spriteFactory;
    }

    /**
     * Get the sprite factory
     * @return the sprite factory
     */
    public SpriteFactory getSpriteFactory() {
        return spriteFactory;
    }

    public LayerNode getLayerNode(Layer layer) {
        return layerNodeMap.get(layer);
    }

    public boolean isTintingColorEnabled() {
        return isTintingColorEnabled;
    }

    public void setTintingColorEnabled(boolean enabled) {
        this.isTintingColorEnabled = enabled;
        for (LayerNode node : layerNodeMap.values()) {
            if (node != null) {
                node.setTintColorEnabled(enabled);
            }
        }
    }

    public Spatial getMapObjectSprite(ObjectGroup layer, MapObject obj) {
        if (!layerNodeMap.containsKey(layer)) {
            return null;
        }
        return layerNodeMap.get(layer).getObjectSprite(obj);
    }

    public abstract Spatial createTileGrid(Material material);

    /**
     * Render the tiled map
     *
     * @return return a Spatial for the whole map.
     */
    public Spatial render() {

        if (tiledMap == null) {
            return null;
        }

        int len = sortedLayers.size();
        for (int i = 0; i < len; i++) {
            Layer layer = sortedLayers.get(i);

            // skip layer not updated
            if (!layer.isNeedUpdated() || (layer instanceof GroupLayer)) {
                continue;
            }

            LayerNode layerNode = layerNodeMap.computeIfAbsent(layer, key -> {
                LayerNode node = new LayerNode(layer);
                node.setTintColorEnabled(isTintingColorEnabled);
                rootNode.attachChild(node);
                return node;
            });

            if (layer instanceof TileLayer) {
                render(layerNode, (TileLayer) layer);
            } else if (layer instanceof ObjectGroup) {
                render(layerNode, (ObjectGroup) layer);
            } else if (layer instanceof ImageLayer) {
                render(layerNode, (ImageLayer) layer);
            }

            layerNode.setYAxis(getLayerYIndex(i));// update y-axis sort
            layerNode.updateLayerParam();// update tint-color and layer-opacity

            layer.setNeedUpdated(false);

            if (layer.isVisible()) {
                rootNode.attachChild(layerNode);
            } else {
                rootNode.detachChild(layerNode);
            }
        }

        return rootNode;
    }

    public abstract void visitTiles(TileVisitor visitor);

    protected void render(LayerNode layerNode, Layer layer) {
        if (layer instanceof TileLayer) {
            render(layerNode, (TileLayer) layer);
        } else if (layer instanceof ObjectGroup) {
            render(layerNode, (ObjectGroup) layer);
        } else if (layer instanceof ImageLayer) {
            render(layerNode, (ImageLayer) layer);
        }
    }

    protected void render(LayerNode layerNode, TileLayer layer) {
        visitTiles((x, y, z) -> {
            if (layer.isNeedUpdateAt(x, y)) {
                final Tile tile = layer.getTileAt(x, y);
                if (tile == null) {
                    layerNode.removeTileSprite(x, y);
                } else {
                    Material material = spriteFactory.newMaterial(tile);
                    Geometry visual = spriteFactory.newTileSprite(tile, material);

                    Vector2f pixelCoord = tileToScreenCoords(x, y);
                    float tileYAxis = getTileYAxis(z);
                    visual.move(pixelCoord.x, tileYAxis, pixelCoord.y);

                    layerNode.setTileSpriteAt(x, y, visual);
                }
                layer.setNeedUpdateAt(x, y, false);
            }
        });
    }

    /**
     * Create the visual part for every ObjectNode in a ObjectLayer.
     *
     * @param layer A ObjectLayer object
     */
    protected void render(LayerNode layerNode, ObjectGroup layer) {
        List<MapObject> objects = layer.getObjects();

        Material material = layerNode.getSpriteMaterial();
        if (material == null) {
            material = spriteFactory.newMaterial(layer.getColor());
            layerNode.setSpriteMaterial(material);
        }

        int len = objects.size();
        objects.sort(layer.getDrawOrder());
        for (int i = 0; i < len; i++) {
            MapObject obj = objects.get(i);

            if (!obj.isNeedUpdate()) {
                continue;
            }

            Spatial sprite = layerNode.getObjectSprite(obj);

            if (obj.isVisible()) {
                if (sprite == null) {
                    sprite = spriteFactory.newObjectSprite(obj, material);
                    if (sprite == null) {
                        logger.warn("Failed creating sprite for object failed, object:{}", obj);
                        obj.setNeedUpdate(false);
                        continue;
                    }
                }

                layerNode.putObjectSprite(obj, sprite);// add sprite to layer-node

                float x = (float) obj.getX();
                float y = (float) obj.getY();

                // sort top-down
                // don't support sorting by index
                float z = getObjectTopDownYIndex(y);

                Vector2f screenCoord = pixelToScreenCoords(x, y);
                sprite.move(screenCoord.x, z, screenCoord.y);

                double deg = obj.getRotation();
                if (deg != 0) {
                    float radian = (float) (FastMath.DEG_TO_RAD * deg);
                    // rotate the spatial clockwise
                    sprite.setLocalRotation(new Quaternion().fromAngles(0, -radian, 0));
                }
                layerNode.attachChild(sprite);
            } else {
                if (sprite != null && sprite.getParent() != null) {
                    layerNode.detachChild(sprite);
                }
            }
            obj.setNeedUpdate(false);
        }
    }

    protected void render(LayerNode layerNode, ImageLayer layer) {
        if (layer.isNeedUpdated()) {

            Spatial sprite = layerNode.getImageSprite();

            if (sprite == null) {
                Material material = spriteFactory.newMaterial(layer.getImage());
                Mesh mesh = spriteFactory.getMeshFactory().rectangle(mapSize.getX(), mapSize.getY(), true);
                Geometry geom = new Geometry(layer.getName(), mesh);
                geom.setMaterial(material);
                layerNode.setMaterial(material);
                layerNode.setImageSprite(geom);

                sprite = geom;
            }

            if (layer.isVisible()) {
                layerNode.attachChild(sprite);
            } else {
                layerNode.detachChild(sprite);
            }

            layer.setNeedUpdated(false);
        }
    }

    public abstract void renderGrid(Node gridVisual, Material gridMaterial);

    ///////////////////////////////////////////////
    ///////// Coordinates System Convert //////////
    ///////////////////////////////////////////////

    /**
     * Convert the pixel coordinates to screen coordinates.
     * @param x the x coordinate in pixel
     * @param y the y coordinate in pixel
     * @return the screen coordinates
     */
    public abstract Vector2f pixelToScreenCoords(float x, float y);

    /**
     * Convert the screen coordinates to pixel coordinates.
     * @param x the x coordinate in screen
     * @param y the y coordinate in screen
     * @return the pixel coordinates
     */
    public abstract Point pixelToTileCoords(float x, float y);

    /**
     * Convert the tile coordinates to pixel coordinates.
     * @param x the x coordinate in tile
     * @param y the y coordinate in tile
     * @return the pixel coordinates
     */
    public abstract Vector2f tileToPixelCoords(float x, float y);

    /**
     * Convert the tile coordinates to screen coordinates.
     * @param x the x coordinate in tile
     * @param y the y coordinate in tile
     * @return the screen coordinates
     */
    public abstract Vector2f tileToScreenCoords(float x, float y);

    /**
     * Convert the screen coordinates to pixel coordinates.
     * @param x the x coordinate in screen
     * @param y the y coordinate in screen
     * @return the pixel coordinates
     */
    public abstract Vector2f screenToPixelCoords(float x, float y);

    /**
     * Convert the screen coordinates to tile coordinates.
     * @param x the x coordinate in screen
     * @param y the y coordinate in screen
     * @return the tile coordinates
     */
    public abstract Point screenToTileCoords(float x, float y);

    /**
     * Get the map node
     * @return the map node
     */
    public Node getRootNode() {
        return rootNode;
    }

    public void updateLayerYAxis() {
        for (Layer layer : sortedLayers) {
            layer.setNeedUpdated(true);
        }
    }

    public void setLayerDistance(double layerDistance) {
        this.layerDistance = layerDistance;
        this.step = layerDistance / (height * width);
        updateLayerYAxis();
    }

    public void setLayerGap(double layerGap) {
        this.layerGap = layerGap;
        updateLayerYAxis();
    }

    public float getLayerYIndex(int index) {
        return  (float) (index *(layerDistance + layerGap));
    }

    /**
     * this is the z-index in the layer
     * @param tileZIndex the z-index in the layer, range from [0 , width * height)
     * @return the y-axis in the layer
     */
    protected float getTileYAxis(int tileZIndex) {
        return (float) (tileZIndex * step);
    }

    public float getObjectTopDownYIndex(float y) {
        float tileY = y / mapSize.getY();
        return (float) (tileY * layerDistance);
    }

    public Point getMapDimension() {
        return mapSize;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }
}
