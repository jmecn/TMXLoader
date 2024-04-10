package com.jme3.tmx.render;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.*;
import com.jme3.tmx.core.*;
import com.jme3.tmx.enums.FillMode;
import com.jme3.tmx.enums.Orientation;
import com.jme3.tmx.render.shape.*;
import com.jme3.tmx.util.ObjectMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;

/**
 * desc: This class is used to create objects for the ObjectGroup
 *
 * @author yanmaoyuan
 */
public class ObjectRenderer {
    private static final Logger logger = LoggerFactory.getLogger(ObjectRenderer.class);
    /**
     * This value used to generate ellipse mesh.
     */
    private static final int ELLIPSE_POINTS = 36;

    private final TiledMap map;
    private final Material mat;
    private final Material bgMat;

    public ObjectRenderer(ObjectGroup layer) {
        this.map = layer.getMap();

        ColorRGBA borderColor = layer.getColor();
        ColorRGBA bgColor = borderColor.mult(0.3f);

        mat = layer.getMaterial();
        bgMat = mat.clone();
        bgMat.setColor("Color", bgColor);

        ColorRGBA tintColor = layer.getTintColor();
        if (tintColor != null) {
            mat.setColor("TintColor", tintColor);
            bgMat.setColor("TintColor", tintColor);
        }
    }

    public Spatial create(MapObject obj) {
        switch (obj.getShape()) {
            case RECTANGLE: {
                rectangle(obj);
                break;
            }
            case ELLIPSE: {
                ellipse(obj);
                break;
            }
            case POLYGON: {
                polygon(obj);
                break;
            }
            case POLYLINE: {
                polyline(obj);
                break;
            }
            case POINT: {
                point(obj);
                break;
            }
            case IMAGE: {
                image(obj);
                break;
            }
            case TILE: {
                tile(obj);
                break;
            }
            case TEXT: {
                text(obj);
                break;
            }
            default: {
                break;
            }

        }

        Spatial visual = obj.getVisual();
        if (visual == null) {
            return null;
        }
        double deg = obj.getRotation();
        if (deg != 0) {
            float radian = (float) (FastMath.DEG_TO_RAD * deg);
            // rotate the spatial clockwise
            visual.rotate(0, -radian, 0);
        }

        return visual;
    }

    private void rectangle(MapObject obj) {
        Mesh borderMesh = new Rect((float)obj.getWidth(), (float)obj.getHeight(), true);
        Mesh backMesh = new Rect((float)obj.getWidth(), (float)obj.getHeight(), false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(borderMesh, map.getTileWidth(), map.getTileHeight());
            ObjectMesh.toIsometric(backMesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry border = new Geometry("border", borderMesh);
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("rectangle", backMesh);
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void ellipse(MapObject obj) {
        Mesh borderMesh = new Ellipse((float)obj.getWidth(), (float)obj.getHeight(), ELLIPSE_POINTS, true);
        Mesh backMesh = new Ellipse((float)obj.getWidth(), (float)obj.getHeight(), ELLIPSE_POINTS, false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(borderMesh, map.getTileWidth(), map.getTileHeight());
            ObjectMesh.toIsometric(backMesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry border = new Geometry("border", borderMesh);
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("ellipse", backMesh);
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void polygon(MapObject obj) {
        Mesh borderMesh = new Polygon(obj.getPoints(), true);
        Mesh backMesh = new Polygon(obj.getPoints(), false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(borderMesh, map.getTileWidth(), map.getTileHeight());
            ObjectMesh.toIsometric(backMesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry border = new Geometry("border", borderMesh);
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("polygon", backMesh);
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void polyline(MapObject obj) {
        Mesh mesh = new Polyline(obj.getPoints(), false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(mesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry visual = new Geometry(obj.getName(), mesh);
        visual.setMaterial(mat);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void point(MapObject obj) {
        Geometry border = new Geometry("border", new Marker(16, ELLIPSE_POINTS, true));
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("marker", new Marker(16, ELLIPSE_POINTS, false));
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void image(MapObject obj) {
        Geometry visual = new Geometry(obj.getName(), new Rect((float)obj.getWidth(), (float)obj.getHeight(), false));
        visual.setMaterial(obj.getMaterial());
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        obj.setVisual(visual);
    }

    private void tile(MapObject obj) {
        // The tile
        Tile tile = obj.getTile();

        Geometry visual = tile.getVisual().clone();
        visual.setName(obj.getName());
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        float th = tile.getHeight();
        float tw = tile.getWidth();
        float offsetX = tile.getTileset().getTileOffsetX();
        float offsetY = tile.getTileset().getTileOffsetY();

        logger.info("map tile width:{}, height:{}, tile width: {}, height: {}", map.getTileWidth(), map.getTileHeight(), tw, th);

        // When the object has a gid set, then it is represented by
        // the image of the tile with that global ID. The image
        // alignment currently depends on the map orientation.

        // In orthogonal, it's aligned to the bottom-left
        float[] vertices = new float[]{
                offsetX,    0, offsetY,
                offsetX+tw, 0, offsetY,
                offsetX+tw, 0, offsetY-th,
                offsetX,    0, offsetY-th};

        // In isometric, it's aligned to the bottom-center.
        if (map.getOrientation() == Orientation.ISOMETRIC) {
            for (int i = 0; i < vertices.length; i += 3) {
                vertices[i] -= tw * 0.5f;
            }
        }

        Mesh mesh = visual.getMesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);

        flip(visual, obj.getTile());

        // scale the tile
        if (tile.getTileset().getFillMode() == FillMode.STRETCH) {
            visual.setLocalScale((float) obj.getWidth() / tile.getWidth(), 1, (float) obj.getHeight() / tile.getHeight());
        }

        obj.setVisual(visual);
    }

    private void text(MapObject obj) {
        // TODO render text
        ObjectText objectText = obj.getTextData();
    }


    /**
     * Flip the tile
     *
     * @param visual The spatial for this tile.
     * @param tile The image of this tile.
     */
    protected void flip(Spatial visual, Tile tile) {
        if (tile.isFlippedHorizontally()) {
            visual.rotate(0, 0, FastMath.PI);
            visual.move(tile.getWidth(), 0, 0);
        }

        if (tile.isFlippedVertically()) {
            visual.rotate(FastMath.PI, 0, 0);
            visual.move(0, 0, tile.getHeight());
        }

        /*
         * <pre>
         * [      *]
         * [    *  ]
         * [  *    ]
         * [*      ]
         * </pre>
         */
        if (tile.isFlippedAntiDiagonally()) {
            // TODO flip diagonally
        }
    }

}
