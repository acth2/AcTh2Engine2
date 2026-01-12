#version 330

in vec2 outTexCoord;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 colour;

void main()
{
    vec4 texel = texture(texture_sampler, outTexCoord);
    fragColor = vec4(colour.rgb, colour.a * texel.a);
}
