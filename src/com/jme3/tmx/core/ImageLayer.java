package com.jme3.tmx.core;

import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

/**
 * A layer consisting of a single image.
 */
public class ImageLayer extends Layer {

	private String source;
	private Texture texture;
	private Material material;
	private Spatial visual;
	
	/**
	 * Default constructor
	 */
	public ImageLayer() {}
	
	public ImageLayer(int width, int height) {
		super(width, height);
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

	public Spatial getVisual() {
		return visual;
	}

	public void setVisual(Spatial visual) {
		this.visual = visual;
	}
}
