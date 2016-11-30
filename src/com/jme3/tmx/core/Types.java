package com.jme3.tmx.core;

public interface Types {

	/**
	 * When you use the tile flipping feature added in Tiled Qt 0.7, the highest
	 * two bits of the gid store the flipped state. Bit 32 is used for storing
	 * whether the tile is horizontally flipped and bit 31 is used for the
	 * vertically flipped tiles. And since Tiled Qt 0.8, bit 30 means whether
	 * the tile is flipped (anti) diagonally, enabling tile rotation. These bits
	 * have to be read and cleared before you can find out which tileset a tile
	 * belongs to.
	 * 
	 * When rendering a tile, the order of operation matters. The diagonal flip
	 * (x/y axis swap) is done first, followed by the horizontal and vertical
	 * flips.
	 */
	// Bits on the far end of the 32-bit global tile ID are used for tile flags
	int FLIPPED_HORIZONTALLY_FLAG = 0x80000000;
	int FLIPPED_VERTICALLY_FLAG = 0x40000000;
	int FLIPPED_DIAGONALLY_FLAG = 0x20000000;

	public enum Origin {
		BottomLeft, BottomCenter
	}
}
