package com.jme3.tmx.app;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.tmx.TiledMapAppState;
import com.jme3.tmx.TmxLoader;
import com.jme3.tmx.core.*;

/**
 * Test update tile
 * @author yanmaoyuan
 *
 */
public class TestUpdateTile extends SimpleApplication {
    private TileLayer tileLayer;

    private TiledMap tiledMap;

    private float time = 0.0f;
    private static final float COOL_DOWN = 0.1f;
    private int tileId = 0;
    private static final int TILE_MAX = 361;

    public void simpleUpdate(float tpf) {
        time += tpf;
        if (time > COOL_DOWN) {
            time -= COOL_DOWN;

            tileId ++;
            if (tileId >= TILE_MAX) {
                tileId = 1;
            }
            tiledMap.setTileAtFromTileId(tileLayer, 0, 0, tileId);
        }
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
    }

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSamples(4);

        TestUpdateTile app = new TestUpdateTile();
        app.setSettings(settings);
        app.start();
    }
}
