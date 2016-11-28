package com.jme3.tiled;

import java.util.logging.Logger;

import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.core.TileLayer;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.tiled.spatial.HexagonalRender;
import com.jme3.tiled.spatial.IsometricRender;
import com.jme3.tiled.spatial.MapRender;
import com.jme3.tiled.spatial.OrthogonalRender;

/**
 * TiledMapAppState will create a Spatial for tile.ore.Map.
 * Only TileLayer will be shown, ObjectGroups are not support for now.
 * @author yanmaoyuan
 *
 */
public class TiledMapAppState extends BaseAppState {

	static Logger logger = Logger.getLogger(TiledMapAppState.class.getName());
	
	private Node rootNode;
	
	private final Map map;
	protected Vector3f centerOffset;
	private MapRender mapRender;

	public TiledMapAppState(Map map) {
		this.map = map;
		float aspect = (float)map.getTileHeight() / map.getTileWidth();
		this.centerOffset = new Vector3f(0.5f, 0.5f*aspect, 0f);
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
        	mapRender = new HexagonalRender(map);
		}
		rootNode = new Node("TileMapRoot");
	}

	public Map getMap() {
		return map;
	}
	
	public Vector3f getLocation(int x, int y) {
		return mapRender.tileLoc2ScreenLoc(x, y);
	}
	
	public Vector3f getCameraLocation(int x, int y) {
		return mapRender.tileLoc2ScreenLoc(x, y).addLocal(centerOffset);
	}
	
	@Override
	protected void initialize(Application app) {
		
		int len = map.getLayerCount();
		for(int i=0; i<len; i++) {
			MapLayer layer = map.getLayer(i);
			
			// skip invisible layer
			if (!layer.isVisible()) {
				continue;
			}
			
			if (layer instanceof TileLayer) {
				rootNode.attachChild(mapRender.createTileLayer((TileLayer) layer));
			}
		}
	}

	@Override
	protected void cleanup(Application app) {
	}

	@Override
	protected void onEnable() {
		((SimpleApplication) getApplication()).getRootNode().attachChild(rootNode);
	}

	@Override
	protected void onDisable() {
		rootNode.removeFromParent();
	}

}
