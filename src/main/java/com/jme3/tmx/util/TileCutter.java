package com.jme3.tmx.util;

import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.tmx.core.Tile;

public class TileCutter {
	
	private int tileWidth;
	private int tileHeight;
	private int tileMargin;
	private int tileSpacing;
	
	private int imageWidth;
	private int imageHeight;
	
	private int nextX = 0;
	private int nextY = 0;
	
	public TileCutter(Texture texture, int width, int height, int margin, int space) {
		this.tileWidth = width;
		this.tileHeight = height;
		this.tileMargin = margin;
		this.tileSpacing = space;
		
		Image image = texture.getImage();
		this.imageWidth = image.getWidth();
		this.imageHeight = image.getHeight();
		
		this.nextX = tileMargin;
		this.nextY = tileMargin;
	}
	
	public Tile getNextTile() {
		
		if (nextY + tileHeight + tileMargin <= imageHeight) {

			Tile tile = new Tile(nextX, nextY, tileWidth, tileHeight);
			
			nextX += tileWidth + tileSpacing;
			if (nextX + tileWidth + tileMargin > imageWidth) {
				nextX = tileMargin;
				nextY += tileHeight + tileSpacing;
			}

			return tile;
		}

		return null;
	}

	public void setTileOffset(int tileOffsetX, int tileOffsetY) {
		this.nextX = tileMargin + tileOffsetX;
		this.nextY = tileMargin + tileOffsetY;
	}
}
