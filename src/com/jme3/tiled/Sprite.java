package com.jme3.tiled;

import tiled.core.Tile;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;

public class Sprite extends Geometry {

	private float width = 1f;
	private float height = 1f;
	
	private float u0 = 0f;
	private float v0 = 0f;
	private float u1 = 1f;
	private float v1 = 1f;
	
	public Sprite(String name) {
		super(name);
		updateMesh();
	}
	
	public Sprite(String name, float width, float height) {
		super(name);
		this.width = width;
		this.height = height;
		updateMesh();
	}
	
	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
		this.updateMesh();
	}
	
	/**
	 * Calculate texCoords for each tile, and create a Geometry for it.
	 */
	public void setTexCoordFromTile(Tile tile) {
		if (tile == null)
			return;
		
		Texture tex = tile.getTexture();
		Image image = tex.getImage();
		float imageWidth = image.getWidth();
		float imageHeight = image.getHeight();
		
		float x = tile.getX();
		float y = tile.getY();
		float tw = tile.getWidth();
		float th = tile.getHeight();

		u0 = x / imageWidth;
		v0 = (imageHeight - y - th + 1) / imageHeight;
		u1 = (x + tw - 1) / imageWidth;
		v1 = (imageHeight - y - 1) / imageHeight;
		
		this.updateMesh();
	}
	
	public void updateMesh() {
		
		float[] vertices = new float[] {
				0, 0, 0,
				width, 0, 0,
				width, height, 0,
				0, height, 0 };
		
		float[] normals = new float[] {
				0, 0, 1,
				0, 0, 1,
				0, 0, 1,
				0, 0, 1 };
		
		short[] indexes = new short[] {
				0, 1, 2,
				0, 2, 3 };
		
		float[] texCoord = new float[] {
				u0, v0,
				u1, v0,
				u1, v1,
				u0, v1 };
		
		Mesh mesh = getMesh();
		if (mesh == null) {
			mesh = new Mesh();
		}
		
		mesh.setBuffer(Type.Position, 3, vertices);
		mesh.setBuffer(Type.TexCoord, 2, texCoord);
		mesh.setBuffer(Type.Normal, 3, normals);
		mesh.setBuffer(Type.Index, 3, indexes);
		mesh.updateBound();
		mesh.setStatic();
		
		this.setMesh(mesh);
	}
}
