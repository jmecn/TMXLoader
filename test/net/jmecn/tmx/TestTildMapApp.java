package net.jmecn.tmx;

import tiled.core.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.tiled.TiledMapAppState;
import com.jme3.tmx.TmxLoader;

public class TestTildMapApp extends SimpleApplication {

	@Override
	public void simpleInitApp() {
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
		Map map = (Map) assetManager.loadAsset("Models/Examples/sewers.tmx");
		
		TiledMapAppState state = new TiledMapAppState(map);
		stateManager.attach(state);
	}

	public static void main(String[] args) {
		TestTildMapApp app = new TestTildMapApp();
		app.start();

	}

}
