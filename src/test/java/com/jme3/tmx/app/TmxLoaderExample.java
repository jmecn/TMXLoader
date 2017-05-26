package com.jme3.tmx.app;

import com.jme3.app.SimpleApplication;
import com.jme3.tmx.TiledMapAppState;
import com.jme3.tmx.TmxLoader;
import com.jme3.tmx.core.TiledMap;

/**
 * Test loading tmx assets with TmxLoader.
 * @author yanmaoyuan
 *
 */
public class TmxLoaderExample extends SimpleApplication {

	@Override
	public void simpleInitApp() {
		// register it
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");

		// load tmx with it
		TiledMap map = (TiledMap) assetManager.loadAsset("Models/Examples/Desert/desert.tmx");
		assert map != null;

		// render it with TiledMapAppState
		stateManager.attach(new TiledMapAppState());
		
		TiledMapAppState tiledMap = stateManager.getState(TiledMapAppState.class);
		tiledMap.setMap(map);
	}

	public static void main(String[] args) {
		TmxLoaderExample app = new TmxLoaderExample();
		app.start();
	}

}
