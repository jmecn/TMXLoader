package com.jme3.tmx;

import java.util.logging.Logger;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.tmx.core.ImageLayer;
import com.jme3.tmx.core.Layer;
import com.jme3.tmx.core.ObjectLayer;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.render.HexagonalRender;
import com.jme3.tmx.render.IsometricRender;
import com.jme3.tmx.render.MapRender;
import com.jme3.tmx.render.OrthogonalRender;
import com.jme3.tmx.render.StaggeredRender;

/**
 * TiledMapAppState will create a Spatial for tile.ore.Map. Only TileLayer will
 * be shown, ObjectGroups are not support for now.
 * 
 * @author yanmaoyuan
 * 
 */
public class TiledMapAppState extends BaseAppState {

	static Logger logger = Logger.getLogger(TiledMapAppState.class.getName());

	private Node rootNode = new Node("TileMapRoot");

	private TiledMap map;
	protected Vector3f centerOffset;
	private MapRender mapRender;

	private ViewPort viewPort;

	public TiledMapAppState() {
	}

	public TiledMapAppState(TiledMap map) {
		setMap(map);
	}

	public TiledMap getMap() {
		return map;
	}

	public Vector3f getLocation(float x, float y) {
		return mapRender.tileLoc2ScreenLoc(x, y);
	}

	public Vector3f getCameraLocation(float x, float y) {
		return mapRender.tileLoc2ScreenLoc(x, y).addLocal(centerOffset);
	}

	@Override
	protected void initialize(Application app) {
		viewPort = app.getViewPort();

		if (map != null)
			render();
	}

	@Override
	protected void cleanup(Application app) {
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

	public void setMap(TiledMap map) {
		this.map = map;
		this.centerOffset = new Vector3f(map.getTileWidth() * 0.5f, 0f, map.getTileHeight() * 0.5f);
		
		switch (map.getOrientation()) {
		case ORTHOGONAL:
			mapRender = new OrthogonalRender(map);
			break;
		case ISOMETRIC:
			mapRender = new IsometricRender(map);
			break;
		case HEXAGONAL:
			mapRender = new HexagonalRender(map);
			break;
		case STAGGERED:
			mapRender = new StaggeredRender(map);
			break;
		}

		mapRender.updateVisual();
	}

	public void render() {
		// background color
		if (map.getBackgroundColor() != null) {
			viewPort.setBackgroundColor(map.getBackgroundColor());
		} else {
			viewPort.setBackgroundColor(ColorRGBA.Black);
		}

		rootNode.detachAllChildren();
		int len = map.getLayerCount();
		for (int i = 0; i < len; i++) {
			Layer layer = map.getLayer(i);

			// skip invisible layer
			if (!layer.isVisible()) {
				continue;
			}

			Spatial visual = null;
			if (layer instanceof TileLayer) {
				visual = mapRender.render((TileLayer) layer);
			}

			if (layer instanceof ObjectLayer) {
				visual = mapRender.render((ObjectLayer) layer);
			}

			if (layer instanceof ImageLayer) {
				visual = mapRender.render((ImageLayer) layer);
			}

			if (visual != null) {
				rootNode.attachChild(visual);
				// this is a little magic to make let top layer block off the
				// bottom layer
				float y = (float) (i+1) / len - 1f;
				visual.setLocalTranslation(0, y, 0);
			}
		}
	}
}
