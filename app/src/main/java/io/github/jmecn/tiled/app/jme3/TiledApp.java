package io.github.jmecn.tiled.app.jme3;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector2f;
import com.jme3.system.awt.AwtPanel;

import io.github.jmecn.tiled.TiledMapAppState;
import io.github.jmecn.tiled.TmxLoader;
import io.github.jmecn.tiled.app.swing.MainWnd;
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

    private final TiledMapAppState tiledMapState;

    public TiledApp(CountDownLatch latch) {
        tiledMapState = new TiledMapAppState();
        tiledMapState.setZoomMode(ZoomMode.CAMERA);
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

        stateManager.attach(tiledMapState);

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
            String status = String.format("Tile: (%d,%d), Pixel: (%.0f, %.0f), Cursor: (%.0f,%.0f)", tile.getX(), tile.getY(), pixel.x, pixel.y, cursor.x, cursor.y);
            wnd.setCursorStatus(status);
        }
    }

    public void load(TiledMap map) {
        enqueue((Callable<Void>)() -> {
            if (map != null) {
                tiledMapState.setMap(map);
                tiledMapState.update(0);

                // look at the center of this map
                tiledMapState.moveToTile(map.getWidth() * 0.5f, map.getHeight() * 0.5f);

                MapRenderer renderer = tiledMapState.getMapRenderer();
                Point mapSize = renderer.getMapDimension();
                String status = String.format("Map[%d,%d], Size:[%d,%d]", map.getWidth(), map.getHeight(), mapSize.getX(), mapSize.getY());
                wnd.setMapStatus(status);
            }
            return null;
        });
    }

    public void setGridVisible(boolean visible) {
        tiledMapState.setGridVisible(visible);
    }

    public void setCursorVisible(boolean visible) {
        tiledMapState.setCursorVisible(visible);
    }

    public void setParallaxEnabled(boolean enabled) {
        tiledMapState.setParallaxEnabled(enabled);
    }

    public boolean isGridVisible() {
        return tiledMapState.isGridVisible();
    }

    public boolean isCursorVisible() {
        return tiledMapState.isCursorVisible();
    }

    public boolean isParallaxEnabled() {
        return tiledMapState.isParallaxEnabled();
    }
}