package me.formercanuck.automationsurvival.world.chunk;

import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.util.FaceGeometry;
import me.formercanuck.automationsurvival.world.Face;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MeshBuilder {
    public static Mesh buildMeshFromVoxelData(int[] voxels, int chunkX, int chunkZ, int chunkSize, int chunkHeight, float voxelSize) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int vertexOffset = 0;

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                for (int y = 0; y < chunkHeight; y++) {
                    int index = x + y * chunkSize + z * chunkSize * chunkHeight;
                    if (voxels[index] == 0) continue;

                    Vector3f pos = new Vector3f(
                            (chunkX * chunkSize + x) * voxelSize,
                            y * voxelSize,
                            (chunkZ * chunkSize + z) * voxelSize
                    );

                    for (Face face : Face.values()) {
                        if (isFaceVisible(voxels, x, y, z, face, chunkSize, chunkHeight)) {
                            float[] color = getColorForFace(face, y, chunkHeight);
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

    private static float[] getColorForFace(Face face, int y, int chunkHeight) {
        return switch (face) {
            case TOP -> new float[]{0.6f, 0.9f, 0.6f};
            case BOTTOM -> new float[]{0.3f, 0.5f, 0.3f};
            default -> {
                float shade = 0.4f + (y / (float) chunkHeight) * 0.4f;
                yield new float[]{shade, shade, shade};
            }
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
