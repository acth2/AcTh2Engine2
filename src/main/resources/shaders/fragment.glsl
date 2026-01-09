#version 460

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;

void main() {
    fragColor = texture(texture_sampler, vTexCoord);
}