package io.github.jmecn.tiled.renderer;

import com.jme3.material.MatParamOverride;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shader.VarType;
import io.github.jmecn.tiled.core.Layer;
import io.github.jmecn.tiled.core.MapObject;
import io.github.jmecn.tiled.core.TileLayer;

import java.util.HashMap;
import java.util.Map;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class LayerNode extends Node {

    private final Layer layer;
    private final Spatial[] tileSprites;// for tile layer
    private final Map<MapObject, Spatial> objectSprites;// for object group
    private Spatial imageSprite;// for image layer

    private Material spriteMaterial;// for image layer and object group

    private final MatParamOverride tintColor;
    private final MatParamOverride layerOpacity;

    public LayerNode(Layer layer) {
        super(layer.getName());
        this.layer = layer;

        tileSprites = new Spatial[layer.getWidth() * layer.getHeight()];// for tile layer
        objectSprites = new HashMap<>();// for object group

        tintColor = new MatParamOverride(VarType.Vector4, MaterialConst.TINT_COLOR, layer.getTintColor());
        layerOpacity = new MatParamOverride(VarType.Float, MaterialConst.LAYER_OPACITY, (float) layer.getOpacity());

        addMatParamOverride(tintColor);
        addMatParamOverride(layerOpacity);

        tintColor.setEnabled(true);
        layerOpacity.setEnabled(true);
    }

    ////// Tile layer methods //////
    public Spatial getTileSpriteAt(int x, int y) {
        TileLayer tileLayer = (TileLayer) layer;
        if (tileLayer.contains(x, y)) {
            int index = y * layer.getWidth() + x;
            return tileSprites[index];
        } else {
            return null;
        }
    }

    /**
     * Sets the tile sprite at the specified position. Does nothing if (x, y) falls outside of this layer.
     *
     * @param x x position of tile
     * @param y y position of tile
     * @param sprite the sprite to place
     */
    public void setTileSpriteAt(int x, int y, Spatial sprite) {
        TileLayer tileLayer = (TileLayer) layer;
        if (tileLayer.contains(x, y)) {
            int index = y * layer.getWidth() + x;
            Spatial old = tileSprites[index];
            if (old != null) {
                detachChild(old);
            }

            attachChild(sprite);
            tileSprites[index] = sprite;
        }
    }

    public void removeTileSprite(int x, int y) {
        TileLayer tileLayer = (TileLayer) layer;
        if (tileLayer.contains(x, y)) {
            int index = y * layer.getWidth() + x;
            if (tileSprites[index] != null) {
                detachChild(tileSprites[index]);
                tileSprites[index] = null;
            }
        }
    }

    ////// Object group methods //////

    public Spatial getObjectSprite(MapObject obj) {
        return objectSprites.get(obj);
    }

    public void putObjectSprite(MapObject obj, Spatial sprite) {
        Spatial old = objectSprites.put(obj, sprite);
        if (old != null && old.getParent() != null) {
            old.removeFromParent();
        }
    }

    public void removeObjectSprite(MapObject obj) {
        Spatial old = objectSprites.remove(obj);
        if (old != null && old.getParent() != null) {
            old.removeFromParent();
        }
    }

    public Material getSpriteMaterial() {
        return spriteMaterial;
    }

    public void setSpriteMaterial(Material spriteMaterial) {
        this.spriteMaterial = spriteMaterial;
    }

    ////// image layer methods //////

    public Spatial getImageSprite() {
        return imageSprite;
    }

    public void setImageSprite(Spatial imageSprite) {
        this.imageSprite = imageSprite;
    }

    public void setYAxis(float y) {
        Vector3f loc = getLocalTranslation();
        setLocalTranslation(loc.x, y, loc.z);
    }

    public void updateLayerParam() {
        tintColor.setValue(layer.getTintColor());
        layerOpacity.setValue((float) layer.getOpacity());
    }

    public void setTintColorEnabled(boolean enabled) {
        tintColor.setEnabled(enabled);
    }

    public void setOpacityEnabled(boolean enabled) {
        layerOpacity.setEnabled(enabled);
    }

}