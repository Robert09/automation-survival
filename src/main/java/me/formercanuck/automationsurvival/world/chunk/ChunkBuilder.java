package me.formercanuck.automationsurvival.world.chunk;

import me.formercanuck.automationsurvival.util.FaceGeometry;
import me.formercanuck.automationsurvival.world.Face;
import me.formercanuck.automationsurvival.world.TerrainNoise;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ChunkBuilder implements Callable<ChunkData> {
    private final int chunkX, chunkZ;
    private final TerrainNoise terrainNoise;

    public ChunkBuilder(int chunkX, int chunkZ, TerrainNoise terrainNoise) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.terrainNoise = terrainNoise;
    }

    @Override
    public ChunkData call() {
        List<Float> vertexData = new ArrayList<>();
        List<Integer> indexData = new ArrayList<>();
        int vertexOffset = 0;
        float voxelSize = 0.1f;
        int CHUNK_SIZE = 16;
        int CHUNK_HEIGHT = 128;

        byte[][][] blocks = new byte[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];

        // Generate terrain
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldX = chunkX * CHUNK_SIZE + x;
                int worldZ = chunkZ * CHUNK_SIZE + z;
                float noise = terrainNoise.getHeight(worldX, worldZ);
                int height = (int) (noise * 10f + 40f);

                for (int y = 0; y < height; y++) {
                    blocks[x][y][z] = 1;
                }
            }
        }

        // Build mesh
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_HEIGHT; y++) {
                    if (blocks[x][y][z] == 0) continue;

                    Vector3f worldPos = new Vector3f(
                            (chunkX * CHUNK_SIZE + x) * voxelSize,
                            y * voxelSize,
                            (chunkZ * CHUNK_SIZE + z) * voxelSize
                    );

                    for (Face face : Face.values()) {
                        float[] color = new float[]{0.4f, 0.8f, 0.4f};
                        float[] faceVertices = FaceGeometry.getFaceVertices(face, worldPos, voxelSize / 2, color);
                        int[] faceIndices = FaceGeometry.getFaceIndices(vertexOffset);

                        for (float v : faceVertices) vertexData.add(v);
                        for (int i : faceIndices) indexData.add(i);
                        vertexOffset += 4;
                    }
                }
            }
        }

        return new ChunkData(toFloatArray(vertexData), toIntArray(indexData), chunkX, chunkZ);
    }

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}
