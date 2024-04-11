#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/Instancing.glsllib"

#ifdef HAS_TILE_OFFSET
uniform vec2 m_TileOffset;
#endif

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;

varying vec2 texCoord;

void main(){
    texCoord = inTexCoord;

    vec3 position = inPosition;
    #ifdef HAS_TILE_OFFSET
    position += vec3(m_TileOffset.x, 0., m_TileOffset.y);
    #endif
    vec4 modelSpacePos = vec4(position, 1.0);

    gl_Position = TransformWorldViewProjection(modelSpacePos);
}