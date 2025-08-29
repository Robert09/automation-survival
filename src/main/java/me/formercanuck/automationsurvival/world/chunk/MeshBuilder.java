package me.formercanuck.automationsurvival.world.chunk;

import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.util.FaceGeometry;
import me.formercanuck.automationsurvival.world.Face;
import me.formercanuck.automationsurvival.world.biomes.BiomeType;
import me.formercanuck.automationsurvival.world.biomes.BiomeWeights;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts voxel data and biome weights into a renderable mesh.
 * Applies face culling and biome-based color blending.
 */
public class MeshBuilder {
    public static Mesh buildMeshFromVoxelData(int[] voxels, BiomeWeights[] biomeMap,
                                              int chunkX, int chunkZ, int chunkSize, int chunkHeight, float voxelSize) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int vertexOffset = 0;

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                BiomeWeights weights = biomeMap[x + z * chunkSize];

                for (int y = 0; y < chunkHeight; y++) {
                    int index = x + y * chunkSize + z * chunkSize * chunkHeight;
                    if (voxels[index] == 0) continue;

                    // World-space position of the voxel
                    Vector3f pos = new Vector3f(
                            (chunkX * chunkSize + x) * voxelSize,
                            y * voxelSize,
                            (chunkZ * chunkSize + z) * voxelSize
                    );

                    for (Face face : Face.values()) {
                        if (isFaceVisible(voxels, x, y, z, face, chunkSize, chunkHeight)) {
                            float[] color = blendBiomeColor(face, y, chunkHeight, weights);
                            float[] faceVerts = FaceGeometry.getFaceVertices(face, pos, voxelSize / 2, color);
                            int[] faceIndices = FaceGeometry.getFaceIndices(vertexOffset);

                            for (float v : faceVerts) vertices.add(v);
                            for (int i : faceIndices) indices.add(i);
                            vertexOffset += 4;
                        }
                    }
                }
            }
        }

        return new Mesh(toFloatArray(vertices), toIntArray(indices));
    }

    /**
     * Determines if a face is exposed by checking its neighbor voxel.
     */
    private static boolean isFaceVisible(int[] voxels, int x, int y, int z, Face face, int chunkSize, int chunkHeight) {
        int nx = x, ny = y, nz = z;
        switch (face) {
            case FRONT -> nz += 1;
            case BACK -> nz -= 1;
            case RIGHT -> nx += 1;
            case LEFT -> nx -= 1;
            case TOP -> ny += 1;
            case BOTTOM -> ny -= 1;
        }

        if (nx < 0 || nx >= chunkSize || ny < 0 || ny >= chunkHeight || nz < 0 || nz >= chunkSize) return true;
        int neighborIndex = nx + ny * chunkSize + nz * chunkSize * chunkHeight;
        return voxels[neighborIndex] == 0;
    }

    /**
     * Blends biome colors based on weights and height.
     */
    private static float[] blendBiomeColor(Face face, int y, int chunkHeight, BiomeWeights weights) {
        float[] blended = new float[3];

        for (Map.Entry<BiomeType, Float> entry : weights.getAll().entrySet()) {
            float[] base = getColor(entry.getKey(), y, chunkHeight);
            blended[0] += base[0] * entry.getValue();
            blended[1] += base[1] * entry.getValue();
            blended[2] += base[2] * entry.getValue();
        }

        return blended;
    }

    /**
     * Returns base color for a biome, modulated by height.
     */
    private static float[] getColor(BiomeType biome, int y, int chunkHeight) {
        float shade = 0.4f + (y / (float) chunkHeight) * 0.4f;

        return switch (biome) {
            case PLAINS    -> new float[]{shade, shade + 0.2f, shade};
            case DESERT    -> new float[]{shade + 0.3f, shade + 0.25f, shade};
            case MOUNTAINS -> new float[]{shade * 0.6f, shade * 0.6f, shade * 0.6f};
            case FOREST    -> new float[]{shade * 0.5f, shade * 0.8f, shade * 0.5f};
            case SWAMP     -> new float[]{shade * 0.3f, shade * 0.5f, shade * 0.4f};
        };
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}