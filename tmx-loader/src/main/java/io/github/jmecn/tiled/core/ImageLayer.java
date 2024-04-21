package io.github.jmecn.tiled.core;

import com.jme3.material.Material;
import com.jme3.texture.Texture;

/**
 * A layer consisting of a single image.
 */
public class ImageLayer extends Layer {

    private TiledImage image;

    private boolean repeatX;
    private boolean repeatY;

    /**
     * Default constructor
     */
    public ImageLayer() {
        // for serialization
    }
    
    public ImageLayer(int width, int height) {
        super(width, height);
    }

    /**
     * @return Whether the image drawn by this layer is repeated along the X axis.
     */
    public boolean isRepeatX() {
        return repeatX;
    }

    /**
     * @param repeatX Whether the image drawn by this layer is repeated along the X axis.
     */
    public void setRepeatX(boolean repeatX) {
        this.repeatX = repeatX;
        setNeedUpdated(true);
    }

    /**
     * @return Whether the image drawn by this layer is repeated along the Y axis.
     */
    public boolean isRepeatY() {
        return repeatY;
    }

    /**
     * @param repeatY Whether the image drawn by this layer is repeated along the Y axis.
     */
    public void setRepeatY(boolean repeatY) {
        this.repeatY = repeatY;
        setNeedUpdated(true);
    }

    /**
     * @return The image of this layer.
     */
    public TiledImage getImage() {
        return image;
    }

    /**
     * @param image The image of this layer.
     */
    public void setImage(TiledImage image) {
        this.image = image;
        // tell map renderer to update it
        setNeedUpdated(true);
    }

}
