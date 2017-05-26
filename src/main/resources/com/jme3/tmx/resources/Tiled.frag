#import "Common/ShaderLib/GLSLCompat.glsllib"

#ifdef TRANS_COLOR
    uniform vec4 m_TransColor;
#endif

uniform vec4 m_Color;
uniform sampler2D m_ColorMap;

varying vec2 texCoord;

void main(){
    vec4 color = vec4(1.0);

    #ifdef HAS_COLORMAP
        color *= texture2D(m_ColorMap, texCoord);     
    #endif

    #ifdef TRANS_COLOR
        if(color.rgb == m_TransColor.rgb) {
            color.a = 0.;
        }
    #endif
    
    #ifdef HAS_COLOR
        color *= m_Color;
    #endif
    
    gl_FragColor = color;
}