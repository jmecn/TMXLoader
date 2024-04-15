package io.github.jmecn.tiled.core;

import com.jme3.material.Material;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;

/**
 * A layer consisting of a single image.
 */
public class ImageLayer extends Layer {

    /**
     * Whether the image drawn by this layer is repeated along the X axis. (since Tiled 1.8)
     */
    private boolean repeatX;
    /**
     * Whether the image drawn by this layer is repeated along the Y axis. (since Tiled 1.8)
     */
    private boolean repeatY;

    private String source;
    private Texture texture;
    private Material material;
    
    /**
     * Default constructor
     */
    public ImageLayer() {}
    
    public ImageLayer(int width, int height) {
        super(width, height);
    }

    public boolean isRepeatX() {
        return repeatX;
    }

    public void setRepeatX(boolean repeatX) {
        this.repeatX = repeatX;
    }

    public boolean isRepeatY() {
        return repeatY;
    }

    public void setRepeatY(boolean repeatY) {
        this.repeatY = repeatY;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    @Override
    public Node getVisual() {
        return (Node) visual;
    }
}
