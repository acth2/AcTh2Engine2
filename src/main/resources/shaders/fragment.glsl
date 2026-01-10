#version 460

in vec2 vTexCoord;
in vec3 vPosition;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec3 colour;
uniform int useColour;

void main()
{
    if (useColour == 1) {
        float r = abs(vPosition.x);
        float g = abs(vPosition.y);
        float b = abs(vPosition.z);
        fragColor = vec4(r, g, b, 1.0);
    } else {
        fragColor = texture(texture_sampler, vTexCoord);
    }
}
