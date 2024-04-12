#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/Instancing.glsllib"

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;

#ifdef USE_TILESET_IMAGE
attribute vec3 inTexCoord2;
uniform vec2 m_ImageSize;
uniform vec4 m_TileSize;
#endif

varying vec2 texCoord;

void main() {
    texCoord = inTexCoord;

    #ifdef USE_TILESET_IMAGE
    // calculate the UV coordinates for the tileset image
    vec2 tilePos = inTexCoord2.xy;
    vec2 uv = (texCoord * m_TileSize.xy + tilePos) / m_ImageSize.xy;
    uv.y = 1.0 - uv.y;
    texCoord = uv;
    #endif

    vec3 position = inPosition;
    vec4 modelSpacePos = vec4(position, 1.0);

    gl_Position = TransformWorldViewProjection(modelSpacePos);
}