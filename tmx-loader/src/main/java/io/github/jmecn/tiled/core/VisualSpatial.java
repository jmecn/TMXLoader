package io.github.jmecn.tiled.core;

import com.jme3.scene.Spatial;

/**
 * This is the visual part for Map, Layers, Tiles and ObjectNodes.
 * scene only update when isSpatialUpdated or isTransformUpdated.
 * 
 * @author yanmaoyuan
 *
 */
public class VisualSpatial {
    
    protected boolean isNeedUpdate = true;
    protected Spatial visual;

    public boolean isNeedUpdated() {
        return isNeedUpdate;
    }

    public void setNeedUpdated(boolean isSpatialUpdated) {
        this.isNeedUpdate = isSpatialUpdated;
    }
    
    public Spatial getVisual() {
        return visual;
    }
    
    /**
     * Set the visual part of a tile map object. 
     * For tiles, basically it is a Geometry with Quad mesh.
     * 
     * @param visual The visual object of this tile.
     */
    public void setVisual(Spatial visual) {
        if (this.visual == visual) {
            return;
        }
        if (this.visual != null) {
            this.visual.removeFromParent();
        }
        this.visual = visual;
        isNeedUpdate = false;
    }
}
