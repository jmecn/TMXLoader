package net.jmecn.tmx;

import tiled.core.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.tiled.RPGCamAppState;
import com.jme3.tiled.TiledMapAppState;
import com.jme3.tmx.TmxLoader;

/**
 * Test TMXLoader and TildeMapAppState
 * @author yanmaoyuan
 *
 */
public class TestTildMapApp extends SimpleApplication {

	static String csvmap = "Models/Examples/csvmap.tmx";
	static String sewers = "Models/Examples/sewers.tmx";
	static String desert = "Models/Examples/Desert/desert.tmx";
	static String mini = "Models/Examples/Hexagonal/hexagonal-mini.tmx";
	static String grass_and_water = "Models/Examples/Isometric/isometric_grass_and_water.tmx";
	static String perspective_walls = "Models/Examples/Orthogonal/perspective_walls.tmx";
	static String outside = "Models/Examples/Orthogonal/orthogonal-outside.tmx";
	
	@Override
	public void simpleInitApp() {
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
		Map map = (Map) assetManager.loadAsset(mini);
		
		TiledMapAppState state = new TiledMapAppState(map);
		stateManager.attach(state);
		
		RPGCamAppState rpgCam = new RPGCamAppState();
		rpgCam.setParallelCamera(24);
		stateManager.attach(rpgCam);
		
		Vector3f location = state.getCameraLocation(12, 12);
		cam.setLocation(location);
		
		viewPort.setBackgroundColor(ColorRGBA.DarkGray);
	}

	public static void main(String[] args) {
		TestTildMapApp app = new TestTildMapApp();
		app.start();

	}

}
