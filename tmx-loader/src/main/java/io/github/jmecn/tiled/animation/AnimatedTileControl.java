package io.github.jmecn.tiled.animation;

import java.nio.FloatBuffer;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.control.AbstractControl;
import io.github.jmecn.tiled.core.Tile;

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
            Mesh mesh = geom.getMesh();

            Tile t = tile.getTileset().getTile(frame.getTileId());
            Mesh tMesh = t.getVisual().getMesh();
            FloatBuffer data = (FloatBuffer)tMesh.getBuffer(Type.TexCoord2).getData();
            mesh.setBuffer(Type.TexCoord2, 3, data);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // ignore
    }
//
//    public Object clone() {
//        AnimatedTileControl control = new AnimatedTileControl(tile);
//        control.anim = anim;
//        return control;
//    }
}
