package com.jme3.tmx.render;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.tmx.core.TiledMap;

public class TiledCamera {

	private TiledMap map;
	protected int width;
	protected int height;
	protected int tileWidth;
	protected int tileHeight;
	
	// The rootNode
	private Node rootNode;
	private Quaternion localRotation;
	
	// How many tiles in a row?
	private float viewColumn;
	
	// The mapNode
	private Vector3f mapTranslation;
	private float mapScale;
	
	private Vector2f screenDimension;
	private Vector2f mapDimension;
	
	
	private MapRenderer renderer = null;
	// with tile currently see?
	private float x;
	private float y;
	
	public TiledCamera(Camera cam, TiledMap map) {

		mapTranslation = new Vector3f();
		mapScale = 1f;

		rootNode = new Node("Tiled Map Root");
		rootNode.setQueueBucket(Bucket.Gui);
		rootNode.attachChild(map.getVisual());

		// translate the scene from XOZ plane to XOY plane
		localRotation = new Quaternion();
		localRotation.fromAngles(FastMath.HALF_PI, 0, 0);
		rootNode.setLocalRotation(localRotation);
		
		// move the rootNode to top-left corner of the screen
		screenDimension = new Vector2f(cam.getWidth(), cam.getHeight());
		rootNode.setLocalTranslation(0, screenDimension.y, 0);
		
		mapDimension = new Vector2f();
	}
	
	private float getMapScale() {
		float pixel = map.getTileWidth() * viewColumn;
		mapScale = screenDimension.x / pixel;
		return mapScale;
	}

	private void getTranslation() {
		Vector2f pos = renderer.tileToScreenCoords(x, y);
		
		pos.x += tileWidth * 0.5f;
		// only for orientation map
		pos.y += tileHeight * 0.5f;
		
		pos.multLocal(getMapScale());
		pos.x  = -pos.x;
		
		Vector2f center = screenDimension.mult(0.5f);
		center.x -= pos.x;
		center.y += pos.y;
	}
}
