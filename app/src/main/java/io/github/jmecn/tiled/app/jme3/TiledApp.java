package io.github.jmecn.tiled.app.jme3;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import com.jme3.app.SimpleApplication;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.system.awt.AwtPanel;

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

    private final ViewAppState viewAppState;

    public TiledApp(CountDownLatch latch) {
        viewAppState = new ViewAppState();
        viewAppState.setZoomMode(ZoomMode.MAP);
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

        stateManager.attach(viewAppState);

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
        if (viewAppState != null && viewAppState.getMapRenderer() != null) {
            Vector2f cursor = inputManager.getCursorPosition();
            Point tile = viewAppState.getCursorTileCoordinate(cursor);
            Vector2f pixel = viewAppState.getCursorPixelCoordinate(cursor);
            wnd.onPick(tile, pixel);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (viewAppState != null && viewAppState.getMapRenderer() != null) {
            Vector2f cursor = inputManager.getCursorPosition();

            Point tile = viewAppState.getCursorTileCoordinate(cursor);
            Vector2f pixel = viewAppState.getCursorPixelCoordinate(cursor);
            Vector2f camPixel = viewAppState.getCameraPixelCoordinate();
            String status = String.format("Tile: (%d,%d), Pixel: (%.0f, %.0f), Cursor: (%.0f,%.0f), Camera:(%d, %d), Camera Center Pixel:(%.0f, %.0f)",
                    tile.getX(), tile.getY(), pixel.x, pixel.y, cursor.x, cursor.y,
                    cam.getWidth(), cam.getHeight(), camPixel.x, camPixel.y);
            wnd.setCursorStatus(status);

            float scale = viewAppState.getMapScale();
            wnd.setMapStatus(String.format("Map Scale: %.1f%%", scale * 100));
        }
    }

    public void load(TiledMap map) {
        enqueue((Callable<Void>)() -> {
            if (map != null) {
                viewAppState.setMap(map);
                viewAppState.update(0);
            }
            return null;
        });
    }

    public void setGridVisible(boolean visible) {
        viewAppState.setGridVisible(visible);
    }

    public void setCursorVisible(boolean visible) {
        viewAppState.setCursorVisible(visible);
    }

    public void setParallaxEnabled(boolean enabled) {
        viewAppState.setParallaxEnabled(enabled);
    }

    public void setTintingColorEnabled(boolean enabled) {
        viewAppState.setTintingColorEnabled(enabled);
    }

    public boolean isGridVisible() {
        return viewAppState.isGridVisible();
    }

    public boolean isCursorVisible() {
        return viewAppState.isCursorVisible();
    }

    public boolean isParallaxEnabled() {
        return viewAppState.isParallaxEnabled();
    }

    public boolean isTintingColorEnabled() {
        return viewAppState.isTintingColorEnabled();
    }
}