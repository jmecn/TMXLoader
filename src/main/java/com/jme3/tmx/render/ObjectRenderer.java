package com.jme3.tmx.render;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.*;
import com.jme3.tmx.enums.Orientation;
import com.jme3.tmx.util.ObjectMesh;

/**
 * desc: This class is used to create objects for the ObjectGroup
 *
 * @author yanmaoyuan
 * @date 2024/4/8
 */
public class ObjectRenderer {
    /**
     * This value used to generate ellipse mesh.
     */
    private static final int ELLIPSE_POINTS = 36;

    private ObjectGroup layer;
    private TiledMap map;

    private Material mat;
    private Material bgMat;

    public ObjectRenderer(ObjectGroup layer) {
        this.layer = layer;
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

    Spatial create(MapObject obj) {
        switch (obj.getShape()) {
            case RECTANGLE: {
                Node visual = rectangle(obj, mat, bgMat);
                obj.setVisual(visual);
                break;
            }
            case ELLIPSE: {
                Node visual = ellipse(obj, mat, bgMat);
                obj.setVisual(visual);
                break;
            }
            case POLYGON: {
                Node visual = polygon(obj, mat, bgMat);
                obj.setVisual(visual);
                break;
            }
            case POLYLINE: {
                Geometry visual = polyline(obj, mat);
                obj.setVisual(visual);
                break;
            }
            case POINT: {
                Node visual = point(obj, mat, bgMat);
                obj.setVisual(visual);
                break;
            }
            case IMAGE: {
                Geometry geom = new Geometry(obj.getName(), ObjectMesh.makeRectangle(obj.getWidth(), obj.getHeight()));
                geom.setMaterial(obj.getMaterial());
                geom.setQueueBucket(RenderQueue.Bucket.Gui);
                obj.setVisual(geom);
                break;
            }
            case TILE: {
                Tile tile = obj.getTile();

                Spatial visual = tile.getVisual().clone();
                visual.setQueueBucket(RenderQueue.Bucket.Gui);

                // flip(visual, obj.getTile());

                // When the object has a gid set, then it is represented by
                // the image of the tile with that global ID. The image
                // alignment currently depends on the map orientation.
                float th = tile.getHeight();
                if (map.getOrientation() == Orientation.ISOMETRIC) {
                    // in isometric it's aligned to the bottom-center.
                    float tw = tile.getWidth();
                    visual.move(0, -tw * 0.5f, -th);
                } else {
                    // In orthogonal orientation it's aligned to the
                    // bottom-left
                    visual.move(0, 0, -th);
                }

                obj.setVisual(visual);
                break;
            }
            case TEXT: {
                // TODO render text
                text(obj, mat, bgMat);
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


    private Node rectangle(MapObject obj, Material mat, Material bgMat) {
        Mesh borderMesh = ObjectMesh.makeRectangleBorder(obj.getWidth(), obj.getHeight());
        Mesh backMesh = ObjectMesh.makeRectangle(obj.getWidth(), obj.getHeight());

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

        return visual;
    }

    private Node ellipse(MapObject obj, Material mat, Material bgMat) {
        Mesh borderMesh = ObjectMesh.makeEllipseBorder(obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS);
        Mesh backMesh = ObjectMesh.makeEllipse(obj.getWidth(), obj.getHeight(), ELLIPSE_POINTS);

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

        return visual;
    }

    private Node polygon(MapObject obj, Material mat, Material bgMat) {
        Mesh borderMesh = ObjectMesh.makePolyline(obj.getPoints(), true);
        Mesh backMesh = ObjectMesh.makePolygon(obj.getPoints());

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

        return visual;
    }

    private Geometry polyline(MapObject obj, Material mat) {
        Mesh mesh = ObjectMesh.makePolyline(obj.getPoints(), false);

        if (map.getOrientation() == Orientation.ISOMETRIC) {
            ObjectMesh.toIsometric(mesh, map.getTileWidth(), map.getTileHeight());
        }

        Geometry geom = new Geometry(obj.getName(), mesh);
        geom.setMaterial(mat);
        geom.setQueueBucket(RenderQueue.Bucket.Gui);

        return geom;
    }

    private Node point(MapObject obj, Material mat, Material bgMat) {
        Geometry border = new Geometry("border", ObjectMesh.makeMarkerBorder(16, ELLIPSE_POINTS));
        border.setMaterial(mat);
        border.setQueueBucket(RenderQueue.Bucket.Gui);

        Geometry back = new Geometry("marker", ObjectMesh.makeMarker(16, ELLIPSE_POINTS));
        back.setMaterial(bgMat);
        back.setQueueBucket(RenderQueue.Bucket.Gui);

        Node visual = new Node(obj.getName());
        visual.attachChild(back);
        visual.attachChild(border);
        visual.setQueueBucket(RenderQueue.Bucket.Gui);

        return visual;
    }

    private void text(MapObject obj, Material mat, Material bgMat) {
        // TODO render text
        ObjectText objectText = obj.getTextData();
    }

}
