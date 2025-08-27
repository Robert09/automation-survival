#version 330 core

in vec3 vColor;
in vec3 vNormal;

uniform vec3 uLightDir;
uniform float uAmbient;

out vec4 fragColor;

uniform sampler2D shadowMap;
in vec4 vLightSpacePos;

float calculateShadow(vec4 lightSpacePos) {
    vec3 projCoords = lightSpacePos.xyz / lightSpacePos.w;
    projCoords = projCoords * 0.5 + 0.5;
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;
    float bias = 0.005;
    float shadow = currentDepth - bias > closestDepth ? 0.2 : 1.0;
    return shadow;
}

void main() {
    float shadow = calculateShadow(vLightSpacePos);
    float diffuse = max(dot(normalize(vNormal), normalize(-uLightDir)), 0.0);
    float brightness = max(uAmbient + diffuse, uAmbient);
    vec3 litColor = vColor * brightness * shadow;
    fragColor = vec4(litColor, 1.0);
}