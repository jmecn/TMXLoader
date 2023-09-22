package com.jme3.tmx.app;

import com.jme3.app.SimpleApplication;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.tmx.TiledMapAppState;
import com.jme3.tmx.TmxLoader;
import com.jme3.tmx.core.TileLayer;
import com.jme3.tmx.core.TiledMap;
import com.jme3.tmx.math2d.Point;

/**
 * Test mouse pick
 * @author yanmaoyuan
 *
 */
public class TestMousePick extends SimpleApplication {
    private TileLayer tileLayer;

    private TiledMap tiledMap;

    private int tileId = 0;
    private static final int TILE_MAX = 361;

    public void click() {
        TiledMapAppState state = stateManager.getState(TiledMapAppState.class);
        Point point = state.getCursorTileCoordinate();
        if (tiledMap.contains(point.x, point.y)) {
            // next tile
            tileId++;
            if (tileId >= TILE_MAX) {
                tileId = 0;
            }
            tiledMap.setTileAtFromTileId(tileLayer, point.x, point.y, tileId);
        }
    }

    public void initInput() {
        inputManager.setCursorVisible(true);
        inputManager.addMapping("CLICK", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    click();
                }
            }
        }, "CLICK");
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");

        tiledMap = (TiledMap) assetManager.loadAsset("Models/Examples/Orthogonal/01.tmx");
        assert tiledMap != null;
        tileLayer = (TileLayer) tiledMap.getLayer("Ground");

        TiledMapAppState tiledMapState = new TiledMapAppState();
        stateManager.attach(tiledMapState);

        tiledMapState.setMap(tiledMap);

        initInput();
    }

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSamples(4);

        TestMousePick app = new TestMousePick();
        app.setSettings(settings);
        app.start();
    }
}