#version 460

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;

out vec2 vTexCoord;

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;

void main() {
    vTexCoord = texCoord;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
