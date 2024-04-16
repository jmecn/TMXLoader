package io.github.jmecn.tiled;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import io.github.jmecn.tiled.loader.TiledMapLoader;
import io.github.jmecn.tiled.loader.TilesetLoader;

import java.io.IOException;

/**
 * Tiled map loader.
 *
 * @author yanmaoyuan
 */
public class TmxLoader implements AssetLoader {

    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        AssetKey<?> key = assetInfo.getKey();
        AssetManager assetManager = assetInfo.getManager();

        String extension = key.getExtension();

        switch (extension) {
            case TiledConst.TMX_EXTENSION:
                TiledMapLoader tiledMapLoader = new TiledMapLoader(assetManager, key);
                return tiledMapLoader.load(assetInfo.openStream());
            case TiledConst.TSX_EXTENSION:
                TilesetLoader tilesetLoader = new TilesetLoader(assetManager, key);
                return tilesetLoader.load(assetInfo.openStream());
            default:
                return null;
        }
    }

}
