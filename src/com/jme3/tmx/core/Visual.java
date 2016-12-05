package com.jme3.tmx.core;

import com.jme3.math.Transform;
import com.jme3.scene.Node;
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
	protected boolean isSpatialUpdated = true;
	protected Spatial visual;

	public boolean isTransformUpdated() {
		return isTransformUpdated;
	}

	public void setTransformUpdated(boolean isTransformUpdated) {
		this.isTransformUpdated = isTransformUpdated;
	}

	public boolean isSpatialUpdated() {
		return isSpatialUpdated;
	}

	public void setSpatialUpdated(boolean isSpatialUpdated) {
		this.isSpatialUpdated = isSpatialUpdated;
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
	 * {@link com.jme3.tmx.render.MapRenderer#createVisual(Tileset)}
	 * {@link com.jme3.tmx.render.MapRenderer#createVisual(ObjectLayer)}
	 * {@link com.jme3.tmx.render.MapRenderer#createVisual(ImageLayer)}
	 * 
	 * @param visual
	 */
	public void setVisual(Spatial visual) {
		
		if (this.visual == visual) {
			return;
		}
		
		Node parent = null;
		Transform transform = null;
		if (this.visual != null) {
			transform = this.visual.getLocalTransform();
			parent = visual.getParent();
			if (parent != null) {
				parent.detachChild(this.visual);
			}
		}
		
		if (visual != null) {
			visual.setLocalTransform(transform);
			parent.attachChild(visual);
		}
		
		this.visual = visual;
		
		isSpatialUpdated = true;
	}
}
