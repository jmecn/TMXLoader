package io.github.jmecn.tiled.render;

import com.jme3.math.ColorRGBA;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class MaterialConst {

    private MaterialConst() {
    }

    public static final String TILED_J3MD = "Shader/Tiled.j3md";

    // uniforms
    public static final String COLOR_MAP = "ColorMap";
    public static final String COLOR = "Color";
    public static final String TRANS_COLOR = "TransColor";
    public static final String USE_TINT_COLOR = "UseTintColor";
    public static final String TINT_COLOR = "TintColor";

    public static final String IMAGE_SIZE = "ImageSize";
    public static final String TILE_SIZE = "TileSize";
    public static final String USE_TILESET_IMAGE = "UseTilesetImage";
    public static final String TILE_POSITION = "TilePosition";
    public static final String USE_TILE_POSITION = "UseTilePosition";

    public static final ColorRGBA CURSOR_AVAILABLE_COLOR = new ColorRGBA(0.7f, 0.7f, 0.9f, 0.5f);
    public static final ColorRGBA CURSOR_UNAVAILABLE_COLOR = new ColorRGBA(0.8f, 0.2f, 0.2f, 0.5f);
}
