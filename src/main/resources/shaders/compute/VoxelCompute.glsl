#version 430

layout(local_size_x = 8, local_size_y = 8, local_size_z = 8) in;

layout(std430, binding = 0) buffer VoxelData {
    int voxels[];
};

uniform int chunkX;
uniform int chunkZ;
uniform int chunkSize;
uniform int chunkHeight;

float getHeight(float x, float z) {
    // Simple noise-based height function
    return 40.0 + sin(x * 0.01) * 10.0 + cos(z * 0.01) * 10.0;
}

void main() {
    ivec3 id = ivec3(gl_GlobalInvocationID.xyz);
    int index = id.x + id.y * chunkSize + id.z * chunkSize * chunkHeight;

    float worldX = float(chunkX * chunkSize + id.x);
    float worldZ = float(chunkZ * chunkSize + id.z);
    float height = getHeight(worldX, worldZ);

    voxels[index] = id.y < int(height) ? 1 : 0;
}