package io.github.jmecn.tiled.renderer;

import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
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

    protected TiledMap tiledMap;
    protected int width;
    protected int height;
    protected int tileWidth;
    protected int tileHeight;

    protected Node rootNode;
    protected List<Layer> sortedLayers;
    protected Map<Layer, Node> layerNodeMap;// save the layer-node relation
    // for tile layer, save the layer tile-spatial relation
    protected Map<Layer, Spatial[]> layerSpatialMap;
    // for object group, save the layer object-spatial relation
    protected Map<Layer, Map<MapObject, Spatial>> objectSpatialMap;
    // for image layer, save the layer image-spatial relation
    protected Map<Layer, Spatial> imageSpatialMap;
    protected Map<Layer, Material> layerMaterialMap;

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
        this.layerSpatialMap = new HashMap<>();
        this.objectSpatialMap = new HashMap<>();
        this.imageSpatialMap = new HashMap<>();
        this.layerMaterialMap = new HashMap<>();
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

    public Node getLayerNode(Layer layer) {
        return layerNodeMap.computeIfAbsent(layer, key -> {
            Node node = new Node(layer.getName());
            rootNode.attachChild(node);
            return node;
        });
    }

    public Spatial[] getLayerSpatials(TileLayer layer) {
        if (layerSpatialMap.containsKey(layer)) {
            return layerSpatialMap.get(layer);
        }
        Spatial[] spatials = new Spatial[layer.getHeight() * layer.getWidth()];
        layerSpatialMap.put(layer, spatials);
        return spatials;
    }

    public Spatial getLayerSpatialAt(TileLayer layer, int x, int y) {
        Spatial[] spatials = getLayerSpatials(layer);
        int index = (y - layer.getY()) * layer.getWidth() + (x - layer.getX());
        return spatials[index];
    }

    /**
     * Sets the spatial at the specified position. Does nothing if (tx, ty) falls
     * outside of this layer.
     *
     * @param layer the layer
     * @param tx x position of tile
     * @param ty y position of tile
     * @param spatial the spatial to place
     */
    public void setSpatialAt(TileLayer layer, int tx, int ty, Spatial spatial) {
        if (layer.contains(tx, ty)) {

            Node parent = getLayerNode(layer);
            Spatial[] spatials = getLayerSpatials(layer);

            int index = (ty - layer.getY()) * layer.getWidth() + (tx - layer.getX());
            Spatial old = spatials[index];
            if (old != null) {
                parent.detachChild(old);
            }

            parent.attachChild(spatial);
            spatials[index] = spatial;

            layer.setNeedUpdateAt(tx, ty, false);

            spriteFactory.setTintColor(spatial, layer.getTintColor());
            spriteFactory.setLayerOpacity(spatial, (float) layer.getOpacity());
        }
    }

    public Map<MapObject, Spatial> getObjectSpatialMap(ObjectGroup layer) {
        if (objectSpatialMap.containsKey(layer)) {
            return objectSpatialMap.get(layer);
        }
        Map<MapObject, Spatial> map = new HashMap<>();
        objectSpatialMap.put(layer, map);
        return map;
    }

    public Spatial getMapObjectSpatial(ObjectGroup layer, MapObject obj) {
        return getObjectSpatialMap(layer).get(obj);
    }

    public Spatial getOrCreateMapObjectSpatial(ObjectGroup layer, MapObject obj, Material material) {
        Map<MapObject, Spatial> objectMap = getObjectSpatialMap(layer);

        Spatial spatial;
        if (objectMap.containsKey(obj)) {
            spatial = objectMap.get(obj);
        } else {
            spatial = spriteFactory.newObjectSprite(obj, material);
            if (spatial == null) {
                logger.warn("Failed creating sprite for object failed, object:{}", obj);
                return null;
            }
            objectMap.put(obj, spatial);
        }
        return spatial;
    }

    public Spatial getImageLayerSpatial(ImageLayer layer) {
        return imageSpatialMap.get(layer);
    }

    public Spatial getOrCreateImageLayerSpatial(ImageLayer layer) {
        if (imageSpatialMap.containsKey(layer)) {
            return imageSpatialMap.get(layer);
        } else {
            Material material = getLayerMaterial(layer);
            Mesh mesh = spriteFactory.getMeshFactory().rectangle(mapSize.getX(), mapSize.getY(), true);
            Geometry geom = new Geometry(layer.getName(), mesh);
            geom.setMaterial(material);
            imageSpatialMap.put(layer, geom);
            return geom;
        }
    }

    private Material getLayerMaterial(ObjectGroup layer) {
        // cache or create material
        Material material = layerMaterialMap.get(layer);
        if (material == null) {
            material = spriteFactory.newMaterial(layer.getColor(), layer.getTintColor());
            layerMaterialMap.put(layer, material);
        } else {
            spriteFactory.setTintColor(material, layer.getTintColor());
        }
        return material;
    }

    private Material getLayerMaterial(ImageLayer layer) {
        // cache or create material
        Material material = layerMaterialMap.get(layer);
        if (material == null) {
            material = spriteFactory.newMaterial(layer.getImage(), layer.getTintColor());
            layerMaterialMap.put(layer, material);
        } else {
            spriteFactory.setTintColor(material, layer.getTintColor());
        }
        return material;
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

            Spatial visual = render(layer);

            if (visual != null) {
                Vector3f loc = visual.getLocalTranslation();
                visual.setLocalTranslation(loc.x, getLayerYIndex(i), loc.z);
                layer.setNeedUpdated(false);
            }

            if (layer.isVisible()) {
                rootNode.attachChild(visual);
            } else {
                rootNode.detachChild(visual);
            }
        }

        return rootNode;
    }

    public abstract void visitTiles(TileVisitor visitor);

    protected Spatial render(Layer layer) {
        if (layer instanceof TileLayer) {
            return render((TileLayer) layer);
        } else if (layer instanceof ObjectGroup) {
            return render((ObjectGroup) layer);
        } else if (layer instanceof ImageLayer) {
            return render((ImageLayer) layer);
        } else {
            return null;
        }
    }

    protected Spatial render(TileLayer layer) {
        Node layerNode = getLayerNode(layer);

        visitTiles((x, y, z) -> {
            if (layer.isNeedUpdateAt(x, y)) {
                final Tile tile = layer.getTileAt(x, y);
                if (tile == null) {
                    removeTileSprite(layer, x, y);
                } else {
                    Vector2f pixelCoord = tileToScreenCoords(x, y);
                    putTileSprite(layer, x, y, getTileYAxis(z), tile, pixelCoord);
                }
            }
        });

        return layerNode;
    }

    /**
     * Create the visual part for every ObjectNode in a ObjectLayer.
     * 
     * @param layer A ObjectLayer object
     * 
     * @return a Spatial for this layer
     */
    protected Spatial render(ObjectGroup layer) {
        List<MapObject> objects = layer.getObjects();
        Node layerNode = getLayerNode(layer);
        Map<MapObject, Spatial> objectMap = getObjectSpatialMap(layer);


        Material material = getLayerMaterial(layer);

        int len = objects.size();
        objects.sort(layer.getDrawOrder());
        for (int i = 0; i < len; i++) {
            MapObject obj = objects.get(i);

            if (!obj.isNeedUpdate()) {
                continue;
            }

            if (obj.isVisible()) {
                Spatial spatial = getOrCreateMapObjectSpatial(layer, obj, material);
                if (spatial == null) {
                    continue;
                }

                spriteFactory.setTintColor(spatial, layer.getTintColor());
                spriteFactory.setLayerOpacity(spatial, (float) layer.getOpacity());

                float x = (float) obj.getX();
                float y = (float) obj.getY();

                // sort top-down
                // don't support sorting by index
                float z = getObjectTopDownYIndex(y);

                Vector2f screenCoord = pixelToScreenCoords(x, y);
                spatial.move(screenCoord.x, z, screenCoord.y);

                double deg = obj.getRotation();
                if (deg != 0) {
                    float radian = (float) (FastMath.DEG_TO_RAD * deg);
                    // rotate the spatial clockwise
                    spatial.setLocalRotation(new Quaternion().fromAngles(0, -radian, 0));
                }
                layerNode.attachChild(spatial);
            } else {
                Spatial spatial = objectMap.get(obj);
                if (spatial != null) {
                    layerNode.detachChild(spatial);
                }
            }
            obj.setNeedUpdate(false);
        }

        return layerNode;
    }

    protected Spatial render(ImageLayer layer) {
        Node layerNode = getLayerNode(layer);

        if (layer.isNeedUpdated()) {

            if (layer.isVisible()) {
                Spatial spatial = getOrCreateImageLayerSpatial(layer);

                spriteFactory.setTintColor(spatial, layer.getTintColor());
                spriteFactory.setLayerOpacity(spatial, (float) layer.getOpacity());

                layerNode.attachChild(spatial);
            } else {
                Spatial spatial = getImageLayerSpatial(layer);
                if (spatial != null) {
                    layerNode.detachChild(spatial);
                }
            }

            layer.setNeedUpdated(false);
        }

        return layerNode;
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

    protected void removeTileSprite(TileLayer layer, int x, int y) {
        Spatial[] spatials = getLayerSpatials(layer);
        int index = (y - layer.getY()) * layer.getWidth() + (x - layer.getX());
        if (spatials[index] != null) {
            Node parent = getLayerNode(layer);
            parent.detachChild(spatials[index]);
            spatials[index] = null;
        }
    }

    protected void putTileSprite(TileLayer layer, int x, int y, float z, Tile tile, Vector2f pixelCoord) {
        Material material = spriteFactory.newMaterial(tile);
        Geometry visual = spriteFactory.newTileSprite(tile, material);
        visual.move(pixelCoord.x, z, pixelCoord.y);
        setSpatialAt(layer, x, y, visual);
    }

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
