package io.github.jmecn.tiled.app.jme3;

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
import io.github.jmecn.tiled.app.swing.MainWnd;
import io.github.jmecn.tiled.core.TiledMap;
import io.github.jmecn.tiled.enums.ZoomMode;

import io.github.jmecn.tiled.math2d.Point;
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
        TmxLoader.registerLoader(assetManager);

        stateManager.attach(tiledMapState);

        inputManager.addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                doClick();
            }
        }, "click");

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

    private void doClick() {
        if (tiledMapState != null && tiledMapState.getMapRenderer() != null) {
            Vector2f cursor = inputManager.getCursorPosition();
            Point tile = tiledMapState.getCursorTileCoordinate(cursor);
            Vector2f pixel = tiledMapState.getCursorPixelCoordinate(cursor);
            wnd.onPick(tile, pixel);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (tiledMapState != null && tiledMapState.getMapRenderer() != null) {
            Vector2f cursor = inputManager.getCursorPosition();

            Point tile = tiledMapState.getCursorTileCoordinate(cursor);
            Vector2f pixel = tiledMapState.getCursorPixelCoordinate(cursor);
            Vector2f camPixel = tiledMapState.getCameraPixelCoordinate();
            String status = String.format("Tile: (%d,%d), Pixel: (%.0f, %.0f), Cursor: (%.0f,%.0f), Camera:(%d, %d), Camera Center Pixel:(%.0f, %.0f)",
                    tile.getX(), tile.getY(), pixel.x, pixel.y, cursor.x, cursor.y,
                    cam.getWidth(), cam.getHeight(), camPixel.x, camPixel.y);
            wnd.setCursorStatus(status);

            float scale = tiledMapState.getMapScale();
            wnd.setMapStatus(String.format("Map Scale: %.1f%%", scale * 100));
        }
    }

    public void load(TiledMap map) {
        enqueue((Callable<Void>)() -> {
            if (map != null) {
                tiledMapState.setMap(map);
                tiledMapState.setMapScale(1f);
                tiledMapState.update(0);
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