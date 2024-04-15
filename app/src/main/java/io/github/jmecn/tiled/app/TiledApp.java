package io.github.jmecn.tiled.app;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import com.jme3.app.SimpleApplication;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.system.awt.AwtPanel;

import io.github.jmecn.tiled.TiledMapAppState;
import io.github.jmecn.tiled.TmxLoader;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.enums.ZoomMode;

import io.github.jmecn.tiled.math2d.Point;
import io.github.jmecn.tiled.render.MapRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author yanmaoyuan
 * 
 */
public class TiledApp extends SimpleApplication {

    static Logger log = LoggerFactory.getLogger(TiledApp.class.getName());

    private final CountDownLatch latch;
    private AwtPanel panel;
    private MainWnd wnd;

    private TiledMapAppState tiledMapState;

    public TiledApp(CountDownLatch latch) {
        super();
        this.latch = latch;
    }

    public void setPanel(AwtPanel panel) {
        this.panel = panel;
    }
    public void setWnd(MainWnd wnd) {
        this.wnd = wnd;
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");

        TiledMapAppState state = new TiledMapAppState();
        state.setZoomMode(ZoomMode.CAMERA);
        stateManager.attach(state);
        this.tiledMapState = state;

        /*
         * Wait until both AWT panels are ready.
         */
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for panels", e);
            Thread.currentThread().interrupt();
            System.exit(-1);
        }

        panel.attachTo(true, viewPort, guiViewPort);
        
        flyCam.setDragToRotate(true);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (tiledMapState != null && tiledMapState.getMapRenderer() != null) {
            Point tile = tiledMapState.getCursorTileCoordinate();
            Vector2f pixel = tiledMapState.getCursorPixelCoordinate();
            Vector2f cursor = tiledMapState.getCursorScreenCoordinate();
            String status = String.format("Tile: (%d,%d), Pixel: (%.0f, %.0f), Cursor: (%.0f,%.0f)", tile.x, tile.y, pixel.x, pixel.y, cursor.x, cursor.y);
            wnd.setCursorStatus(status);
        }
    }

    public void load(final String assetPath) {
        enqueue((Callable<Void>) () -> {
            TiledMap map = null;
            try {
                map = (TiledMap) assetManager.loadAsset(assetPath);
            } catch (Exception e) {
                // i don't care
            }

            if (map != null) {
                tiledMapState.setMap(map);
                tiledMapState.update(0);

                // look at the center of this map
                tiledMapState.moveToTile(map.getWidth() * 0.5f, map.getHeight() * 0.5f);

                // update the window title
                wnd.setTitle("Tiled Map Viewer - " + assetPath);

                MapRenderer renderer = tiledMapState.getMapRenderer();
                Point mapSize = renderer.getMapDimension();
                String status = String.format("Map[%d,%d], Size:[%d,%d]", map.getWidth(), map.getHeight(), mapSize.x, mapSize.y);
                wnd.setMapStatus(status);
            } else {
                wnd.setTitle("Failed to load " + assetPath);
            }
            return null;
        });

    }

    public void toggleGrid() {
        TiledMapAppState tiledMap = stateManager.getState(TiledMapAppState.class);
        tiledMap.toggleGrid();
    }

}