package io.github.jmecn.tiled.app;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import io.github.jmecn.tiled.TiledMapAppState;
import io.github.jmecn.tiled.TmxLoader;
import io.github.jmecn.tiled.core.TiledMap;

/**
 * Test loading tmx assets with TmxLoader.
 * @author yanmaoyuan
 *
 */
public class TmxLoaderExample extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        // register it
        assetManager.registerLoader(TmxLoader.class, "tmx", "tsx");

        // load tmx with it
        TiledMap map = (TiledMap) assetManager.loadAsset("Desert/desert.tmx");

        // render it with TiledMapAppState
        stateManager.attach(new TiledMapAppState());

        TiledMapAppState tiledMap = stateManager.getState(TiledMapAppState.class);
        tiledMap.setMap(map);
        //tiledMap.setViewColumn(20);
    }

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSamples(4);
        settings.setGammaCorrection(false);

        TmxLoaderExample app = new TmxLoaderExample();
        app.setSettings(settings);
        app.start();
    }

}
