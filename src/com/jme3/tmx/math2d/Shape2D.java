package com.jme3.tmx.math2d;

import com.jme3.math.Vector2f;

public interface Shape2D {
	boolean contains(Vector2f point);
	boolean contains(float x, float y);
}
