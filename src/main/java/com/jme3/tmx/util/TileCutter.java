package com.jme3.tmx.util;

import com.jme3.tmx.core.Tile;

public class TileCutter {
    
    private final int tileWidth;
    private final int tileHeight;
    private final int tileMargin;
    private final int tileSpacing;
    
    private final int imageWidth;
    private final int imageHeight;
    
    private int nextX;
    private int nextY;
    
    public TileCutter(int imageWidth, int imageHeight, int width, int height, int margin, int space) {
        this.tileWidth = width;
        this.tileHeight = height;
        this.tileMargin = margin;
        this.tileSpacing = space;
        
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        
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
}
