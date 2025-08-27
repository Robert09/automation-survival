#version 330 core

in vec3 vColor;
in vec3 vNormal;

uniform vec3 uLightDir;

out vec4 fragColor;

uniform sampler2D shadowMap;
in vec4 vLightSpacePos;

float calculateShadow(vec4 lightSpacePos) {
    vec3 projCoords = lightSpacePos.xyz / lightSpacePos.w;
    projCoords = projCoords * 0.5 + 0.5;
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;
    float bias = 0.005;
    float shadow = currentDepth - bias > closestDepth ? 0.5 : 1.0;
    return shadow;
}

void main() {
    float shadow = calculateShadow(vLightSpacePos);
    float brightness = max(dot(normalize(vNormal), normalize(-uLightDir)), 0.2);
    vec3 litColor = vColor * brightness * shadow;
    fragColor = vec4(litColor, 1.0);
}