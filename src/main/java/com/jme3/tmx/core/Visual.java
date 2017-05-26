package com.jme3.tmx.core;

import com.jme3.scene.Spatial;

/**
 * This is the visual part for Map, Layers, Tiles and ObjectNodes.
 * 
 * scene only update when isSpatialUpdated or isTransformUpdated.
 * 
 * @author yanmaoyuan
 *
 */
public class Visual {
	
	protected boolean isTransformUpdated = true;
	protected boolean isNeedUpdate = true;
	protected Spatial visual;

	public boolean isTransformUpdated() {
		return isTransformUpdated;
	}

	public void setTransformUpdated(boolean isTransformUpdated) {
		this.isTransformUpdated = isTransformUpdated;
	}

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
	 * 
	 * For tiles, basically it is a Geometry with Quad mesh.
	 * 
	 * This method is called by
	 * {@link com.jme3.tmx.render.MapRenderer#render(TileLayer)}
	 * {@link com.jme3.tmx.render.MapRenderer#render(ObjectLayer)}
	 * {@link com.jme3.tmx.render.MapRenderer#render(ImageLayer)}
	 * 
	 * @param visual The Spatial of this tile.
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
