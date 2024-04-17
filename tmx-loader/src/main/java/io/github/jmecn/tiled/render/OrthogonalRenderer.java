package io.github.jmecn.tiled.render;

import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TileLayer;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.enums.RenderOrder;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.render.grid.OrthoGrid;
import io.github.jmecn.tiled.render.shape.Rect;

/**
 * Orthogonal render
 * 
 * @author yanmaoyuan
 *
 */
public class OrthogonalRenderer extends MapRenderer {

    public OrthogonalRenderer(TiledMap map) {
        super(map);
    }

    @Override
    public Spatial createTileGrid(Material material) {
        Mesh mesh = new Rect(tileWidth, tileHeight, false);
        Geometry geom = new Geometry("TileGrid", mesh);
        geom.setMaterial(material);
        return geom;
    }

    @Override
    public Spatial render(TileLayer layer) {
        int startX = 0;
        int startY = 0;
        int endX = width - 1;
        int endY = height - 1;

        int incX = 1;
        int incY = 1;
        int tmp;
        RenderOrder renderOrder = map.getRenderOrder();
        switch (renderOrder) {
            case RIGHT_UP: {
                // swap y
                tmp = endY;
                endY = startY;
                startY = tmp;
                incY = -1;
                break;
            }
            case LEFT_DOWN: {
                // swap x
                tmp = endX;
                endX = startX;
                startX = tmp;
                incX = -1;
                break;
            }
            case LEFT_UP: {
                // swap x
                tmp = endX;
                endX = startX;
                startX = tmp;
                incX = -1;

                // swap y
                tmp = endY;
                endY = startY;
                startY = tmp;
                incY = -1;
                break;
            }
            case RIGHT_DOWN: {
                break;
            }
        }
        endX += incX;
        endY += incY;

        int tileZIndex = 0;

        // instance the layer node
        if (layer.getVisual() == null) {
            Node layerNode = new Node("TileLayer#" + layer.getName());
            layerNode.setQueueBucket(Bucket.Gui);
            layer.setVisual(layerNode);
            layer.getParentVisual().attachChild(layerNode);
        }
        
        for (int y = startY; y != endY; y += incY) {
            for (int x = startX; x != endX; x += incX) {
                final Tile tile = layer.getTileAt(x, y);
                if (tile == null || tile.getVisual() == null) {
                    continue;
                }

                if (layer.isNeedUpdateAt(x, y)) {
                    Geometry visual = copySprite(tile);
                    flip(visual, tile);
                    Vector2f pixelCoord = tileToScreenCoords(x, y);
                    visual.move(pixelCoord.x, tileZIndex, pixelCoord.y);
                    visual.setQueueBucket(Bucket.Gui);
                    layer.setSpatialAt(x, y, visual);
                }
                
                tileZIndex++;
            }
        }
        // make it thinner
        if (tileZIndex > 0) {
            layer.getVisual().setLocalScale(1, 1f / tileZIndex, 1);
        }

        return layer.getVisual();
    }

    @Override
    public void renderGrid(Node gridVisual, Material gridMaterial) {
        // add boundary
        OrthoGrid grid = new OrthoGrid(width, height, tileWidth, tileHeight);
        Geometry geom = new Geometry("Grid#Boundary", grid);
        geom.setMaterial(gridMaterial);
        gridVisual.attachChild(geom);
    }

    // Coordinates System Convert

    // OrthogonalRenderer, StaggeredRenderer, HexagonalRenderer
    @Override
    public Vector2f pixelToScreenCoords(float x, float y) {
        return new Vector2f(x, y);
    }

    @Override
    public Point pixelToTileCoords(float x, float y) {
        return new Point(x / tileWidth, y / tileHeight);
    }

    @Override
    public Vector2f tileToPixelCoords(float x, float y) {
        return new Vector2f(x * tileWidth, y * tileHeight);
    }

    @Override
    public Vector2f tileToScreenCoords(float x, float y) {
        return new Vector2f(x * tileWidth, y * tileHeight);
    }

    @Override
    public Vector2f screenToPixelCoords(float x, float y) {
        return new Vector2f(x, y);
    }

    @Override
    public Point screenToTileCoords(float x, float y) {
        return new Point(x / tileWidth, y / tileHeight);
    }

}