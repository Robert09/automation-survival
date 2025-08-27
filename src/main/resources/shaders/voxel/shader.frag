#version 330 core

in vec3 vColor;
in vec3 vNormal;

uniform vec3 uLightDir;

out vec4 fragColor;

void main() {
    float brightness = max(dot(normalize(vNormal), normalize(-uLightDir)), 0.2);
    vec3 litColor = vColor * brightness;
    fragColor = vec4(litColor, 1.0);
}