package net.jmecn.tmx;

import junit.framework.TestCase;

import org.junit.Test;

import tiled.core.Map;
import tiled.core.TileLayer;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.shader.plugins.GLSLLoader;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.tmx.TmxLoader;

public class TestTMXLoader extends TestCase {

	// Orthogonal Map
	static String csvmap = "Models/Examples/csvmap.tmx";
	static String sewers = "Models/Examples/sewers.tmx";
	static String desert = "Models/Examples/Desert/desert.tmx";
	static String outside = "Models/Examples/Orthogonal/orthogonal-outside.tmx";
	static String perspective_walls = "Models/Examples/Orthogonal/perspective_walls.tmx";
	
	// Hexagonal Map
	static String hexagonal = "Models/Examples/hexagonal.tmx";
	static String mini = "Models/Examples/Hexagonal/hexagonal-mini.tmx";
	
	// Staggered Map
	static String staggered = "Models/Examples/staggered.tmx";
	
	// Isometric Map
	static String grass_and_water = "Models/Examples/Isometric/isometric_grass_and_water.tmx";
	
	static AssetManager assetManager;
	
	static {
		assetManager = new DesktopAssetManager();
		assetManager.registerLocator("/", ClasspathLocator.class);
		assetManager.registerLoader(J3MLoader.class, "j3md");
		assetManager.registerLoader(GLSLLoader.class, "vert", "frag", "geom", "tsctrl", "tseval", "glsl", "glsllib");
		assetManager.registerLoader(AWTLoader.class, "jpg", "bmp", "gif", "png", "jpeg");
		assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");
	}
	
    @Test
    public void testReadingExampleMap() throws Exception {

        // Act
        Map map = (Map) assetManager.loadAsset(sewers);

        // Assert
        assertEquals(Map.Orientation.ORTHOGONAL, map.getOrientation());
        assertEquals(50, map.getHeight());
        assertEquals(50, map.getHeight());
        assertEquals(24, map.getTileWidth());
        assertEquals(24, map.getTileHeight());
        assertEquals(3, map.getLayerCount());
        assertNotNull(((TileLayer)map.getLayer(0)).getTileAt(0, 0));
    }

    @Test
    public void testReadingExampleCsvMap() throws Exception {
        // Act
        Map map = (Map) assetManager.loadAsset(csvmap);

        // Assert
        assertEquals(Map.Orientation.ORTHOGONAL, map.getOrientation());
        assertEquals(100, map.getHeight());
        assertEquals(100, map.getHeight());
        assertEquals(32, map.getTileWidth());
        assertEquals(32, map.getTileHeight());
        assertEquals(1, map.getLayerCount());
        assertNotNull(((TileLayer)map.getLayer(0)).getTileAt(0, 0));
    }

    @Test
    public void testReadingExampleHexagonalMap() throws Exception {
        // Act
        Map map = (Map) assetManager.loadAsset(hexagonal);

        // Assert
        assertEquals(Map.Orientation.HEXAGONAL, map.getOrientation());
        assertEquals(9, map.getHeight());
        assertEquals(9, map.getHeight());
        assertEquals(32, map.getTileWidth());
        assertEquals(32, map.getTileHeight());
        assertEquals(16, map.getHexSideLength());
        assertEquals(Map.StaggerAxis.Y, map.getStaggerAxis());
        assertEquals(Map.StaggerIndex.ODD, map.getStaggerIndex());
        assertEquals(1, map.getLayerCount());
    }

    @Test
    public void testReadingExampleStaggeredMap() throws Exception {
        // Act
        Map map = (Map) assetManager.loadAsset(staggered);

        // Assert
        assertEquals(Map.Orientation.STAGGERED, map.getOrientation());
        assertEquals(9, map.getHeight());
        assertEquals(9, map.getHeight());
        assertEquals(32, map.getTileWidth());
        assertEquals(32, map.getTileHeight());
        assertEquals(Map.StaggerAxis.Y, map.getStaggerAxis());
        assertEquals(Map.StaggerIndex.ODD, map.getStaggerIndex());
        assertEquals(1, map.getLayerCount());
    }

    @Test
    public void testReadingExampleIsometricMap() throws Exception {
        // Act
        Map map = (Map) assetManager.loadAsset(grass_and_water);

        // Assert
        assertEquals(Map.Orientation.ISOMETRIC, map.getOrientation());
        assertEquals(25, map.getHeight());
        assertEquals(25, map.getHeight());
        assertEquals(64, map.getTileWidth());
        assertEquals(32, map.getTileHeight());
        assertEquals(1, map.getLayerCount());
    }
}