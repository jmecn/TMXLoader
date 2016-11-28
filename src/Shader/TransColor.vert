#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/Instancing.glsllib"

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;

varying vec2 texCoord;

void main(){
    #ifdef HAS_COLORMAP
        texCoord = inTexCoord;
    #endif

    vec4 modelSpacePos = vec4(inPosition, 1.0);

    gl_Position = TransformWorldViewProjection(modelSpacePos);
}