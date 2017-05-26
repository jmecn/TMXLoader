package com.jme3.tmx.animation;

import java.nio.FloatBuffer;
import java.util.logging.Logger;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.control.AbstractControl;
import com.jme3.tmx.core.Tile;

/**
 * This control used to play animation of a tile.
 * 
 * @author yanmaoyuan
 * 
 */
public class AnimatedTileControl extends AbstractControl {

	static Logger logger = Logger
			.getLogger(AnimatedTileControl.class.getName());

	private Tile tile;
	private Animation anim;
	private int currentFrameIndex;
	private float unusedTime;

	public AnimatedTileControl(Tile tile) {
		this.tile = tile;
		resetAnimation();
		
		// TODO currently just set it to the first animation
		setAnim(0);
	}

	public void setAnim(String name) {
		anim = tile.getAnimation(name);
		resetAnimation();
	}

	public void setAnim(int index) {
		anim = tile.getAnimations().get(0);
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
		int previousTileId = frame.tileId;

		while (frame.duration > 0 && unusedTime > frame.duration) {
			unusedTime -= frame.duration;
			currentFrameIndex = (currentFrameIndex + 1) % anim.getTotalFrames();

			frame = anim.getFrame(currentFrameIndex);
		}

		/*
		 * whether this caused the current tileId to change.
		 */
		if (previousTileId != frame.tileId) {
			Geometry geom = (Geometry) spatial;
			Mesh mesh = geom.getMesh();

			Tile t = tile.getTileset().getTile(frame.tileId);
			Mesh tMesh = ((Geometry) t.getVisual()).getMesh();
			FloatBuffer data = (FloatBuffer)tMesh.getBuffer(Type.TexCoord).getData();
			mesh.setBuffer(Type.TexCoord, 2, data);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	public Object clone() {
		AnimatedTileControl control = new AnimatedTileControl(tile);
		control.anim = anim;
		return control;
	}
}
