#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/Instancing.glsllib"

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inColor;

varying vec2 texCoord;

// Scroll uv by time
uniform float g_Time;
uniform float m_Speed;

void main(){
    #ifdef HAS_COLORMAP
    	vec2 uv = inTexCoord;
    	uv.x += m_Speed * g_Time * 0.1;
        texCoord = uv;
    #endif

    vec4 modelSpacePos = vec4(inPosition, 1.0);

    gl_Position = TransformWorldViewProjection(modelSpacePos);
}