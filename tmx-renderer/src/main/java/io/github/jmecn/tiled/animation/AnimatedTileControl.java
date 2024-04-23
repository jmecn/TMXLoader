package io.github.jmecn.tiled.animation;

import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import io.github.jmecn.tiled.core.Tile;
import io.github.jmecn.tiled.render.MaterialConst;

/**
 * This control used to play animation of a tile.
 * 
 * @author yanmaoyuan
 * 
 */
public class AnimatedTileControl extends AbstractControl {

    private final Tile tile;
    private Animation anim;
    private int currentFrameIndex;
    private float unusedTime;

    public AnimatedTileControl(Tile tile) {
        this.tile = tile;
        resetAnimation();
        setAnim(0);
    }

    public void setAnim(String name) {
        anim = tile.getAnimation(name);
        resetAnimation();
    }

    public void setAnim(int index) {
        anim = tile.getAnimations().get(index);
        resetAnimation();
    }

    /**
     * Resets the tile animation.
     */
    public void resetAnimation() {
        currentFrameIndex = 0;
        unusedTime = 0f;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // no animation
        if (anim == null) {
            return;
        }

        float ms = tpf * 1000;
        unusedTime += ms;
        Frame frame = anim.getFrame(currentFrameIndex);
        int previousTileId = frame.getTileId();

        while (frame.getDuration() > 0 && unusedTime > frame.getDuration()) {
            unusedTime -= frame.getDuration();
            currentFrameIndex = (currentFrameIndex + 1) % anim.getTotalFrames();

            frame = anim.getFrame(currentFrameIndex);
        }

        /*
         * whether this caused the current tileId to change.
         */
        if (previousTileId != frame.getTileId()) {
            Geometry geom = (Geometry) spatial;

            Tile t = tile.getTileset().getTile(frame.getTileId());
            Vector2f position = new Vector2f(t.getX(), t.getY());
            geom.getMaterial().setVector2(MaterialConst.TILE_POSITION, position);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // ignore
    }

}
