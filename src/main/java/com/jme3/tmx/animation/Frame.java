package com.jme3.tmx.animation;

/**
 * a single frame of an animated tile
 * 
 * @author yanmaoyuan
 * 
 */
public class Frame {
    /**
     * The local ID of a tile within the parent tileset.
     */
    public int tileId;
    /**
     * How long (in milliseconds) this frame should be displayed before
     * advancing to the next frame.
     */
    public int duration;
}