package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.util.Color;
import me.formercanuck.automationsurvival.util.FaceGeometry;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Chunk {

    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_HEIGHT = 256;

    private final byte[][][] blocks = new byte[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
    private final Renderer renderer;
    private final int chunkX, chunkZ;

    private boolean isVisible = true;

    private TerrainNoise terrainNoise;

    public Chunk(TerrainNoise terrainNoise, Renderer renderer, int chunkX, int chunkZ) {
        this.renderer = renderer;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        this.terrainNoise = terrainNoise;

        generateTerrain();
        buildMesh();
    }

    private void generateTerrain() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldX = chunkX * CHUNK_SIZE + x;
                int worldZ = chunkZ * CHUNK_SIZE + z;

                // Get noise value and normalize to height range
                float noiseValue = terrainNoise.getHeight(worldX, worldZ); // [-1, 1]
                int height = (int) ((noiseValue + 1f) * 0.5f * 64) + 40;   // [40, 104]

                for (int y = 0; y < CHUNK_HEIGHT; y++) {
                    if (y < height - 4) {
                        blocks[x][y][z] = 2; // stone
                    } else if (y < height - 1) {
                        blocks[x][y][z] = 1; // dirt
                    } else if (y == height) {
                        blocks[x][y][z] = 3; // grass
                    } else {
                        blocks[x][y][z] = 0; // air
                    }
                }
            }
        }
    }

    private void buildMesh() {
        List<Float> vertexData = new ArrayList<>();
        List<Integer> indexData = new ArrayList<>();
        int vertexOffset = 0;
        float voxelSize = 0.1f;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_HEIGHT; y++) {
                    byte block = blocks[x][y][z];
                    if (block == 0) continue;

                    Set<Face> visibleFaces = getVisibleFaces(x, y, z);
                    float[] color = getBlockColor(block);
                    Vector3f worldPos = new Vector3f(
                            (chunkX * CHUNK_SIZE + x) * voxelSize,
                            y * voxelSize,
                            (chunkZ * CHUNK_SIZE + z) * voxelSize
                    );

                    for (Face face : visibleFaces) {
                        float[] faceVertices = FaceGeometry.getFaceVertices(face, worldPos, voxelSize / 2, color);
                        int[] faceIndices = FaceGeometry.getFaceIndices(vertexOffset);
                        for (float v : faceVertices) vertexData.add(v);
                        for (int i : faceIndices) indexData.add(i);
                        vertexOffset += 4;
                    }
                }
            }
        }

        Mesh chunkMesh = new Mesh(toFloatArray(vertexData), toIntArray(indexData));
        renderer.meshes.add(chunkMesh);
    }

    private Set<Face> getVisibleFaces(int x, int y, int z) {
        Set<Face> faces = EnumSet.noneOf(Face.class);
        if (isAir(x, y, z + 1)) faces.add(Face.FRONT);
        if (isAir(x, y, z - 1)) faces.add(Face.BACK);
        if (isAir(x - 1, y, z)) faces.add(Face.LEFT);
        if (isAir(x + 1, y, z)) faces.add(Face.RIGHT);
        if (isAir(x, y + 1, z)) faces.add(Face.TOP);
        if (isAir(x, y - 1, z)) faces.add(Face.BOTTOM);
        return faces;
    }

    private float[] getBlockColor(byte block) {
        return switch (block) {
            case 1 -> Color.BROWN.toArray();
            case 2 -> Color.GRAY.toArray();
            case 3 -> Color.GREEN.toArray();
            default -> Color.BLACK.toArray();
        };
    }

    private boolean isAir(int x, int y, int z) {
        return getBlock(x, y, z) == 0;
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_HEIGHT || z < 0 || z >= CHUNK_SIZE) {
            return 0;
        }
        return blocks[x][y][z];
    }

    public byte getHighestBlock(int x, int z) {
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[x][y][z] != 0) return blocks[x][y][z];
        }
        return 0;
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

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}