#import "Common/ShaderLib/GLSLCompat.glsllib"

#if defined(DISCARD_ALPHA)
    uniform float m_AlphaDiscardThreshold;
#endif

uniform vec4 m_Color;
uniform sampler2D m_ColorMap;

varying vec2 texCoord;

void main(){
    vec4 color = vec4(1.0);

    #ifdef HAS_COLORMAP
        color *= texture2D(m_ColorMap, texCoord);     
    #endif

    #ifdef HAS_COLOR
        color *= m_Color;
    #endif
    
    #if defined(DISCARD_ALPHA)
        if(color.a < m_AlphaDiscardThreshold){
           discard;
        }
    #endif
    
    gl_FragColor = color;
}