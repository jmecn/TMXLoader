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

varying vec2 texCoord;

void main(){
    vec4 color = vec4(1.0);

    #ifdef HAS_COLOR_MAP
    vec2 uv = texCoord;
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