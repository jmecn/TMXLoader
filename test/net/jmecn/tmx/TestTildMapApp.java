package net.jmecn.tmx;

import tiled.core.Map;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.tiled.TiledMapAppState;
import com.jme3.tmx.TmxLoader;

public class TestTildMapApp extends SimpleApplication {

	static String csvmap = "Models/Examples/csvmap.tmx";
	static String sewers = "Models/Examples/sewers.tmx";
	static String desert = "Models/Examples/Desert/desert.tmx";
	static String mini = "Models/Examples/Hexagonal/hexagonal-mini.tmx";
	static String grass_and_water = "Models/Examples/Isometric/isometric_grass_and_water.tmx";
	static String perspective_walls = "Models/Examples/Orthogonal/perspective_walls.tmx";
	
	@Override
	public void simpleInitApp() {
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
		Map map = (Map) assetManager.loadAsset(mini);
		
		TiledMapAppState state = new TiledMapAppState(map);
		state.setViewColumns(8);
		stateManager.attach(state);
		
		createAxis();
		
		Vector3f location = state.tileLoc2ScreenLoc(3, 4);
		// move camera to the left top of the map
		cam.setLocation(location);
		
		createBox(location);
	}

	private void createAxis() {
		Grid gird = new Grid(101, 101, 1);
		Geometry gem = new Geometry("axis", gird);
		gem.rotate(-FastMath.HALF_PI, 0, 0);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Green);
		gem.setMaterial(mat);
		rootNode.attachChild(gem);
		
		Box box = new Box(0.1f, 0.1f, 0.1f);
		Geometry geom = new Geometry("orgin", box);
		
		mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Red);
		geom.setMaterial(mat);
		rootNode.attachChild(geom);
	}
	
	private void createBox(Vector3f location) {
		Box box = new Box(0.1f, 0.1f, 0.1f);
		Geometry geom = new Geometry("orgin", box);
		geom.setLocalTranslation(location);
		
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Red);
		geom.setMaterial(mat);
		rootNode.attachChild(geom);
	}
	
	public static void main(String[] args) {
		TestTildMapApp app = new TestTildMapApp();
		app.start();

	}

}
