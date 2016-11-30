package com.jme3.tmx.core;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

/**
 * A layer consisting of a single image.
 */
public class ImageLayer extends Layer {

	private String imageSource;
	private ColorRGBA transparentColor;
	private Texture texture;
}
