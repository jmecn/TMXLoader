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
public class TestTiledMapApp extends SimpleApplication {
	
	static String orthogonal_01 = "Models/Examples/Orthogonal/01.tmx";
	static String orthogonal_02 = "Models/Examples/Orthogonal/02.tmx";
	static String orthogonal_03 = "Models/Examples/Orthogonal/03.tmx";
	static String orthogonal_04 = "Models/Examples/Orthogonal/04.tmx";
	static String orthogonal_05 = "Models/Examples/Orthogonal/05.tmx";
	static String orthogonal_06 = "Models/Examples/Orthogonal/06.tmx";
	static String orthogonal_07 = "Models/Examples/Orthogonal/07.tmx";
	static String orthogonal_08 = "Models/Examples/Orthogonal/orthogonal-outside.tmx";
	static String orthogonal_09 = "Models/Examples/Orthogonal/perspective_walls.tmx";
	static String orthogonal_10 = "Models/Examples/csvmap.tmx";
	static String orthogonal_11 = "Models/Examples/sewers.tmx";
	static String orthogonal_12 = "Models/Examples/Desert/desert.tmx";
	
	static String isometric_01 = "Models/Examples/Isometric/01.tmx";
	static String isometric_02 = "Models/Examples/Isometric/02.tmx";
	static String isometric_03 = "Models/Examples/Isometric/03.tmx";
	static String isometric_04 = "Models/Examples/Isometric/isometric_grass_and_water.tmx";
	
	static String hexagonal_01 = "Models/Examples/Hexagonal/01.tmx";
	static String hexagonal_02 = "Models/Examples/Hexagonal/02.tmx";
	static String hexagonal_03 = "Models/Examples/Hexagonal/03.tmx";
	static String hexagonal_04 = "Models/Examples/Hexagonal/04.tmx";
	static String hexagonal_05 = "Models/Examples/Hexagonal/05.tmx";
	static String hexagonal_06 = "Models/Examples/Hexagonal/hexagonal-mini.tmx";
	
	static String staggered_01 = "Models/Examples/Staggered/01.tmx";
	static String staggered_02 = "Models/Examples/Staggered/02.tmx";
	static String staggered_03 = "Models/Examples/Staggered/03.tmx";
	static String staggered_04 = "Models/Examples/Staggered/04.tmx";
	static String staggered_05 = "Models/Examples/Staggered/05.tmx";
	
	@Override
	public void simpleInitApp() {
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
		Map map = (Map) assetManager.loadAsset(staggered_05);
		
		TiledMapAppState state = new TiledMapAppState(map);
		stateManager.attach(state);
		
		RPGCamAppState rpgCam = new RPGCamAppState();
		rpgCam.setParallelCamera(24);
		stateManager.attach(rpgCam);
		
		Vector3f location = state.getCameraLocation(0, 0);
		cam.setLocation(location);
		
		viewPort.setBackgroundColor(ColorRGBA.DarkGray);
	}

	public static void main(String[] args) {
		TestTiledMapApp app = new TestTiledMapApp();
		app.start();

	}

}
