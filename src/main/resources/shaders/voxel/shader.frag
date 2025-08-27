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
    projCoords = clamp(projCoords, 0.0, 1.0); // Prevent out-of-bounds sampling

    float closestDepth = texture(shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;
    float bias = 0.01; // Slightly increased to reduce shadow acne

    float shadow = currentDepth - bias > closestDepth ? 0.5 : 1.0;
    return shadow;
}

void main() {
    float shadow = calculateShadow(vLightSpacePos);
    float diffuse = max(dot(normalize(vNormal), normalize(-uLightDir)), 0.0);

    vec3 ambientColor = vColor * uAmbient;
    vec3 diffuseColor = vColor * diffuse * shadow;

    vec3 litColor = ambientColor + diffuseColor;
    fragColor = vec4(vColor * uAmbient, 1.0);
}