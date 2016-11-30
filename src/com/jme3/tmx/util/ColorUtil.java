package com.jme3.tmx.util;

import com.jme3.math.ColorRGBA;

public class ColorUtil {

	public static ColorRGBA toColorRGBA(String str) {
		float scalor = 1f / 255f;
		
		// #AARRGGBB || AARRGGBB
		if (str.startsWith("#")) {
			str = str.substring(1);
		}

		int argb = Integer.parseInt(str, 16);
		int alpha = (argb >> 24) & 0xFF;
		int red = (argb >> 16) & 0xFF;
		int green = (argb >> 8) & 0xFF;
		int blue = (argb >> 0) & 0xFF;
		ColorRGBA color = new ColorRGBA(red * scalor, green * scalor,
				blue * scalor, alpha * scalor);

		return color;
	}
}
