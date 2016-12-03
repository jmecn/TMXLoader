package com.jme3.tmx;

import java.util.logging.Logger;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.Layer;
import com.jme3.tmx.core.ObjectLayer;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.math2d.Point;
import com.jme3.tmx.render.HexagonalRenderer;
import com.jme3.tmx.render.IsometricRenderer;
import com.jme3.tmx.render.MapRenderer;
import com.jme3.tmx.render.OrthogonalRenderer;
import com.jme3.tmx.render.StaggeredRenderer;

/**
 * TiledMapAppState will create a Spatial for tile.ore.Map. Only TileLayer will
 * be shown, ObjectGroups are not support for now.
 * 
 * @author yanmaoyuan
 * 
 */
public class TiledMapAppState extends BaseAppState {

	static Logger logger = Logger.getLogger(TiledMapAppState.class.getName());

	private Node rootNode;
	private Quaternion localRotation;
	private Vector3f localTranslation;

	private boolean dirty = true;
	
	int screenWidth;
	int screenHeight;
	
	private TiledMap map;
	protected Vector3f centerOffset;
	private MapRenderer mapRenderer;

	private ViewPort viewPort;

	public TiledMapAppState() {
		this(null);
	}

	public TiledMapAppState(TiledMap map) {
		localTranslation = new Vector3f();
		localRotation = new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0);
		
		rootNode = new Node("Tiled Map Root");
		rootNode.setQueueBucket(Bucket.Gui);
		rootNode.setLocalRotation(localRotation);
		
		setMap(map);
	}

	public TiledMap getMap() {
		return map;
	}

	public MapRenderer getMapRenderer() {
		return mapRenderer;
	}
	
	public Vector3f getLocation(float x, float y) {
		Vector2f pos = mapRenderer.tileToScreenCoords(x, y);
		return new Vector3f(pos.x , pos.y, 999-map.getLayerCount());
	}

	public Vector3f getCameraLocation(float x, float y) {
		return mapRenderer.tileLoc2ScreenLoc(x, y).addLocal(centerOffset);
	}

	@Override
	protected void initialize(Application app) {
		viewPort = app.getViewPort();
		
		screenWidth = app.getCamera().getWidth();
		screenHeight = app.getCamera().getHeight();
	}

	@Override
	protected void cleanup(Application app) {
		rootNode.detachAllChildren();
	}

	@Override
	protected void onEnable() {
		((SimpleApplication) getApplication()).getRootNode().attachChild(
				rootNode);
	}

	@Override
	protected void onDisable() {
		rootNode.removeFromParent();
	}
	
	@Override
	public void update(float tpf) {
		if (dirty) {
			render();
			dirty = false;
		}
	}

	public void setMap(TiledMap map) {
		if (map == null)
			return;
		
		if (this.map != map) {
			this.map = map;
		}
		
		switch (map.getOrientation()) {
		case ORTHOGONAL:
			mapRenderer = new OrthogonalRenderer(map);
			break;
		case ISOMETRIC:
			mapRenderer = new IsometricRenderer(map);
			break;
		case HEXAGONAL:
			mapRenderer = new HexagonalRenderer(map);
			break;
		case STAGGERED:
			mapRenderer = new StaggeredRenderer(map);
			break;
		}

		mapRenderer.updateVisual();
		dirty = true;
	}

	/**
	 * Render the tiled map
	 */
	public Node render() {
		
		if (map == null) {
			return null;
		}
		
		// set background color
		if (viewPort != null) {
			ColorRGBA bgColor = map.getBackgroundColor();
			if (bgColor == null) {
				bgColor = ColorRGBA.Black.clone();
			}
			viewPort.setBackgroundColor(bgColor);
		}

		rootNode.detachAllChildren();
		int len = map.getLayerCount();
		int layerCnt = 0;
		for (int i = 0; i < len; i++) {
			Layer layer = map.getLayer(i);

			// skip invisible layer
			if (!layer.isVisible()) {
				continue;
			}

			Spatial visual = null;
			if (layer instanceof TileLayer) {
				visual = mapRenderer.render((TileLayer) layer);
			}

			if (layer instanceof ObjectLayer) {
				visual = mapRenderer.render((ObjectLayer) layer);
			}

			if (layer instanceof ImageLayer) {
				visual = mapRenderer.render((ImageLayer) layer);
			}

			if (visual != null) {
				visual.setQueueBucket(Bucket.Gui);
				rootNode.attachChild(visual);
				
				// this is a little magic to make let top layer block off the
				// bottom layer
				visual.setLocalTranslation(0, layerCnt++, 0);
			}
		}
		
		// make the whole map thinner
		if (layerCnt > 0) {
			rootNode.setLocalScale(1, 1f / layerCnt, 1);
		}
		
		// move it to the left bottom of screen space
		Point mapSize = mapRenderer.getSize();
		localTranslation.set((screenWidth-mapSize.x) * 0.5f, (screenHeight+mapSize.y)*0.5f, 0);
		
		System.out.println(screenWidth + "," + screenHeight + " ->" + localTranslation);
		rootNode.setLocalTranslation(localTranslation);
		return rootNode;
	}
	
	public void moveToTile(float x, float y) {
		Vector2f position = mapRenderer.tileToScreenCoords(x, y);
	}

}
