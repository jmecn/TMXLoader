package net.jmecn.tmx;

import tiled.core.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.tiled.TiledMapAppState;
import com.jme3.tmx.TmxLoader;

public class TestTildMapApp extends SimpleApplication {

	static String csvmap = "Models/Examples/csvmap.tmx";
	static String sewers = "Models/Examples/sewers.tmx";
	static String desert = "Models/Examples/Desert/desert.tmx";
	@Override
	public void simpleInitApp() {
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
		Map map = (Map) assetManager.loadAsset(desert);
		
		TiledMapAppState state = new TiledMapAppState(map);
		state.setViewColumns(40);
		stateManager.attach(state);
		
		// move camera to left top of the map
		cam.setLocation(new Vector3f(0.5f, map.getHeight() - 0.5f, 0));
	}

	public static void main(String[] args) {
		TestTildMapApp app = new TestTildMapApp();
		app.start();

	}

}
