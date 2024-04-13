#import "Common/ShaderLib/GLSLCompat.glsllib"

#ifdef HAS_TRANS_COLOR
uniform vec4 m_TransColor;
#endif
#ifdef HAS_TINT_COLOR
uniform vec4 m_TintColor;
#endif
#ifdef HAS_COLOR
uniform vec4 m_Color;
#endif
#ifdef HAS_COLOR_MAP
uniform sampler2D m_ColorMap;
#endif

#ifdef USE_TILESET_IMAGE
uniform vec2 m_ImageSize;
uniform vec4 m_TileSize;//(width, height, margin, space)
varying vec2 v_TilePos;
#endif

varying vec2 v_TexCoord;

vec2 getTileUVClamped(vec2 tilePos, vec2 tileSize, vec2 imageSize) {
    vec2 pixel = v_TexCoord * tileSize + tilePos;
    vec2 min = vec2(tilePos + 0.5);
    vec2 max = vec2(tilePos + tileSize - 0.5);
    vec2 uv = clamp(pixel, min, max) / imageSize;
    uv.y = 1.0 - uv.y;
    return uv;
}

void main(){
    vec4 color = vec4(1.0);

    #ifdef HAS_COLOR_MAP
    vec2 uv = v_TexCoord;

    #ifdef USE_TILESET_IMAGE
    uv = getTileUVClamped(v_TilePos, m_TileSize.xy, m_ImageSize.xy);
    #endif

    color *= texture2D(m_ColorMap, uv);
    #endif

    #ifdef HAS_TRANS_COLOR
    if(color.rgb == m_TransColor.rgb) {
        color.a = 0.;
    }
    #endif

    #ifdef HAS_COLOR
    color *= m_Color;
    #endif

    #ifdef HAS_TINT_COLOR
    color *= m_TintColor;
    #endif

    gl_FragColor = color;
}