#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/Instancing.glsllib"

attribute vec3 inPosition;
attribute vec2 inTexCoord;

#ifdef USE_TILESET_IMAGE
// use texcoord2 as tile position
attribute vec3 inTexCoord2;
// pass it to fragment shader
varying vec2 v_TilePos;
#endif

varying vec2 v_TexCoord;

void main() {
    v_TexCoord = inTexCoord;

    #ifdef USE_TILESET_IMAGE
    v_TilePos = inTexCoord2.xy;
    #endif

    vec3 position = inPosition;
    vec4 modelSpacePos = vec4(position, 1.0);

    gl_Position = TransformWorldViewProjection(modelSpacePos);
}