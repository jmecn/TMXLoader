package io.github.jmecn.tiled.render;

import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.core.TileLayer;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.render.grid.IsoGrid;
import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.render.grid.IsoRect;

/**
 * Isometric render
 * 
 * @author yanmaoyuan
 *
 */
public class IsometricRenderer extends MapRenderer {

    public IsometricRenderer(TiledMap map) {
        super(map);
        
        int side = width + height;
        mapSize.set(side * tileWidth * 0.5f, side * tileHeight * 0.5f);
    }

    @Override
    public Spatial createTileGrid(Material material) {
        // create a grid
        IsoRect mesh = new IsoRect(tileWidth, tileHeight, false);
        Geometry geom = new Geometry("TileGrid", mesh);
        geom.setMaterial(material);
        return geom;
    }

    @Override
    public Spatial render(TileLayer layer) {
        // instance the layer node
        if (layer.getVisual() == null) {
            Node layerNode = new Node("TileLayer#" + layer.getName());
            layerNode.setQueueBucket(Bucket.Gui);
            layer.setVisual(layerNode);
            layer.getParentVisual().attachChild(layerNode);
        }
        
        int tileZIndex = 0;
        
        for(int p = 0; p < height + width - 1; p++) {
            for(int y = 0; y <= p; y++) {
                int x = p - y;
                if(y < height && x < width) {
                    final Tile tile = layer.getTileAt(x, y);
                    if (tile == null || tile.getVisual() == null) {
                        continue;
                    }

                    updateTile(layer, x, y, tileZIndex, tile);
                    
                    tileZIndex++;
                }
            }
        }
        // make it thinner
        if (tileZIndex > 0) {
            layer.getVisual().setLocalScale(1, 1f / tileZIndex, 1);
        }
        return layer.getVisual();
    }

    private void updateTile(TileLayer layer, int x, int y, int z, Tile tile) {
        if (layer.isNeedUpdateAt(x, y)) {
            Spatial visual = tile.getVisual().clone();
            visual.setQueueBucket(Bucket.Gui);

            flip(visual, tile);

            Vector2f pixelCoord = tileToScreenCoords(x, y);
            pixelCoord.x -= tileWidth * 0.5f;// move left to center the tile
            visual.move(pixelCoord.x, z, pixelCoord.y);
            layer.setSpatialAt(x, y, visual);
        }
    }

    @Override
    public void renderGrid(Node gridVisual, Material gridMaterial) {
        // add boundary
        IsoGrid grid = new IsoGrid(width, height, tileWidth, tileHeight);
        Geometry geom = new Geometry("Grid#Boundary", grid);
        geom.setMaterial(gridMaterial);
        gridVisual.attachChild(geom);
    }

    // Coordinates System Convert
    @Override
    public Point pixelToTileCoords(float x, float y) {
        return new Point(x / tileHeight, y / tileHeight);
    }

    @Override
    public Vector2f tileToPixelCoords(float x, float y) {
        return new Vector2f(x * tileHeight, y * tileHeight);
    }

    @Override
    public Point screenToTileCoords(float x, float y) {
        x -= height * tileWidth * 0.5f;
        float tileY = y / tileHeight;
        float tileX = x / tileWidth;
        
        return new Point(tileY + tileX, tileY - tileX);
    }
    
    @Override
    public Vector2f tileToScreenCoords(float x, float y) {
        return new Vector2f((height + x - y) * tileWidth * 0.5f, (x + y) * tileHeight * 0.5f);
    }
    
    @Override
    public Vector2f screenToPixelCoords(float x, float y) {
        
        x -= height * tileWidth * 0.5f;
        float tileY = y / tileHeight;
        float tileX = x / tileWidth;

        return new Vector2f((tileY + tileX) * tileHeight,
                       (tileY - tileX) * tileHeight);
    }

    @Override
    public Vector2f pixelToScreenCoords(float x, float y) {
        
        final float tileY = y / tileHeight;
        final float tileX = x / tileHeight;
        
        return new Vector2f((height + tileX - tileY) * tileWidth * 0.5f, (tileX + tileY) * tileHeight * 0.5f);
    }

}
