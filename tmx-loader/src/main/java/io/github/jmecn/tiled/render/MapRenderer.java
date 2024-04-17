package io.github.jmecn.tiled.render;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.*;
import io.github.jmecn.tiled.core.*;
import io.github.jmecn.tiled.enums.DrawOrder;
import io.github.jmecn.tiled.enums.Orientation;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.render.shape.Rect;
import io.github.jmecn.tiled.render.shape.TileMesh;

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
 * The Point(x,y) in Tiled now converted to Vector3f(x, 0, y).
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
 * 
 */
public abstract class MapRenderer {

    protected TiledMap map;
    protected int width;
    protected int height;
    protected int tileWidth;
    protected int tileHeight;

    /**
     * The whole map size in pixel
     */
    protected Point mapSize;

    protected MapRenderer(TiledMap map) {
        this.map = map;
        this.width = map.getWidth();
        this.height = map.getHeight();
        this.tileWidth = map.getTileWidth();
        this.tileHeight = map.getTileHeight();

        this.mapSize = new Point(width * tileWidth, height * tileHeight);
    }

    public abstract Spatial createTileGrid(Material material);

    /**
     * Render the tiled map
     * 
     * @return return a Spatial for the whole map.
     */
    public Spatial render() {

        if (map == null) {
            return null;
        }

        int len = map.getLayerCount();
        for (int i = 0; i < len; i++) {
            Layer layer = map.getLayer(i);

            // skip invisible layer
            if (!layer.isVisible() || !layer.isNeedUpdated()) {
                continue;
            }

            Spatial visual = null;
            if (layer instanceof TileLayer) {
                visual = render((TileLayer) layer);
            }

            if (layer instanceof ObjectGroup) {
                visual = render((ObjectGroup) layer);
            }

            if (layer instanceof ImageLayer) {
                visual = render((ImageLayer) layer);
            }

            if (layer instanceof GroupLayer) {
                visual = render((GroupLayer) layer);
            }

            if (visual != null) {
                // this is a little magic to make let top layer block off the
                // bottom layer
                visual.setLocalTranslation(0, i, 0);
                layer.setNeedUpdated(false);
            }
        }

        return map.getVisual();
    }

    private void setTintColor(Material material, Layer layer) {
        ColorRGBA tintColor = layer.getTintColor();
        if (tintColor != null) {
            material.setColor("TintColor", tintColor);
        }
    }

    protected Spatial render(GroupLayer group) {
        // instance the layer node
        if (group.getVisual() == null) {
            Node layerNode = new Node("GroupLayer#" + group.getName());
            layerNode.setQueueBucket(Bucket.Gui);
            group.setVisual(layerNode);
            group.getParentVisual().attachChild(layerNode);
        }

        for (Layer layer : group.getLayers()) {
            if (!layer.isVisible() || !layer.isNeedUpdated()) {
                continue;
            }

            Spatial visual = null;
            if (layer instanceof TileLayer) {
                visual = render((TileLayer) layer);
            }

            if (layer instanceof ObjectGroup) {
                visual = render((ObjectGroup) layer);
            }

            if (layer instanceof ImageLayer) {
                visual = render((ImageLayer) layer);
            }

            if (layer instanceof GroupLayer) {
                visual = render((GroupLayer) layer);
            }

            if (visual != null) {
                group.getVisual().attachChild(visual);
            }
        }

        return group.getVisual();
    }

    protected abstract Spatial render(TileLayer layer);

    /**
     * Create the visual part for every ObjectNode in a ObjectLayer.
     * 
     * @param layer A ObjectLayer object
     * 
     * @return a Spatial for this layer
     */
    protected Spatial render(ObjectGroup layer) {
        List<MapObject> objects = layer.getObjects();
        // instance the layer node
        if (layer.getVisual() == null) {
            Node layerNode = new Node("ObjectGroup#" + layer.getName());
            layerNode.setQueueBucket(Bucket.Gui);
            layer.setVisual(layerNode);
            layer.getParentVisual().attachChild(layerNode);
        }

        final ColorRGBA borderColor = layer.getColor();
        final ColorRGBA bgColor = borderColor.mult(0.3f);
        Material mat = layer.getMaterial();
        Material bgMat = mat.clone();
        bgMat.setColor("Color", bgColor);
        // set tint color
        setTintColor(mat, layer);
        setTintColor(bgMat, layer);

        int len = objects.size();

        if (len > 0) {
            layer.getVisual().setLocalScale(1f, 1f / len, 1f);

            // sort draw order
            if (Objects.requireNonNull(layer.getDrawOrder()) == DrawOrder.TOPDOWN) {
                objects.sort(new CompareTopdown());
            } else if (layer.getDrawOrder() == DrawOrder.INDEX) {
                objects.sort(new CompareIndex());
            }
        }

        ObjectRenderer objectRenderer = new ObjectRenderer(layer);
        for (int i = 0; i < len; i++) {
            MapObject obj = objects.get(i);

            if (obj.isVisible() && obj.isNeedUpdated()) {

                Spatial visual = objectRenderer.create(obj);
                if (visual == null) {
                    continue;
                }

                float x = (float) obj.getX();
                float y = (float) obj.getY();

                Vector2f screenCoord = pixelToScreenCoords(x, y);
                visual.move(screenCoord.x, i, screenCoord.y);
                layer.getVisual().attachChild(visual);
            }
        }

        return layer.getVisual();
    }

    protected Spatial render(ImageLayer layer) {
        // instance the layer node
        if (layer.getVisual() == null) {
            Node layerNode = new Node("ImageLayer#" + layer.getName());
            layerNode.setQueueBucket(Bucket.Gui);
            layer.setVisual(layerNode);
            layer.getParentVisual().attachChild(layerNode);
        }

        Material mat = layer.getMaterial();
        setTintColor(mat, layer);

        Mesh mesh = new Rect(mapSize.getX(), mapSize.getY(), false);
        Geometry geom = new Geometry(layer.getName(), mesh);
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Gui);

        layer.getVisual().attachChild(geom);

        return layer.getVisual();
    }

    public abstract void renderGrid(Node gridVisual, Material gridMaterial);

    /******************************
     * Coordinates System Convert *
     ******************************/

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

    protected Geometry copySprite(Tile tile) {
        return tile.getVisual().clone();
    }

    /**
     * Flip the tile
     *
     * @param visual The spatial for this tile.
     * @param tile The image of this tile.
     */
    protected void flip(Geometry visual, Tile tile) {
        if (tile.getGidNoMask() == tile.getGid()) {
            // no flip
            return;
        }
        TileMesh mesh = (TileMesh) visual.getMesh();
        TileMesh newMesh = new TileMesh(mesh.getCoord(), mesh.getSize(), mesh.getOffset(), mesh.getOrigin(), tile.getGid(), map.getOrientation());
        visual.setMesh(newMesh);
    }

    private static final class CompareTopdown implements Comparator<MapObject> {
        @Override
        public int compare(MapObject o1, MapObject o2) {
            double a = o1.getY();
            double b = o2.getY();

            return Double.compare(a, b);
        }
    }

    private static final class CompareIndex implements Comparator<MapObject> {
        @Override
        public int compare(MapObject o1, MapObject o2) {
            int a = o1.getId();
            int b = o2.getId();

            return Integer.compare(a, b);
        }
    }

    public Point getMapDimension() {
        return new Point(mapSize.getX(), mapSize.getY());// read only
    }

    public Vector2f getMapDimensionF() {
        return new Vector2f(mapSize.getX(), mapSize.getY());
    }
}
