package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.util.Color;
import me.formercanuck.automationsurvival.util.Constants;
import me.formercanuck.automationsurvival.util.FaceGeometry;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Chunk {

    public static final int CHUNK_SIZE = 16;
    private static final int CHUNK_HEIGHT = 256;

    private final byte[][][] blocks = new byte[CHUNK_SIZE][CHUNK_HEIGHT][CHUNK_SIZE];
    private final Renderer renderer;
    public final int chunkX, chunkZ;
    private final TerrainNoise terrainNoise;

    private Mesh mesh;

    public boolean isVisible = true;

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

                float noiseValue = terrainNoise.getHeight(worldX, worldZ); // [-1, 1]
                int height = (int) ((noiseValue + 1f) * 0.5f * 64) + 40;   // [40, 104]

                for (int y = 0; y < CHUNK_HEIGHT; y++) {
                    if (y < height - 4) {
                        blocks[x][y][z] = 2; // stone
                    } else if (y < height - 1) {
                        blocks[x][y][z] = 1; // dirt
                    } else {
                        blocks[x][y][z] = 0; // air
                    }

                    if (y == getHighestBlock(x, z)) {
                        blocks[x][y][z] = 3; // grass
                    }
                }
            }
        }
    }

    public void buildMesh() {
        List<Float> vertexData = new ArrayList<>();
        List<Integer> indexData = new ArrayList<>();
        int vertexOffset = 0;
        float voxelSize = Constants.VOXEL_SIZE;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_HEIGHT; y++) {
                    byte block = blocks[x][y][z];
                    if (block == 0) continue;

                    Set<Face> visibleFaces = getVisibleFaces(x, y, z);
                    Vector3f worldPos = new Vector3f(
                            (chunkX * CHUNK_SIZE + x) * voxelSize,
                            y * voxelSize,
                            (chunkZ * CHUNK_SIZE + z) * voxelSize
                    );

                    for (Face face : visibleFaces) {
                        int occlusion = getOcclusion(x, y, z, face);
                        float[] baseColor = getBlockColor(block);
                        float[] shadedColor = applyAO(baseColor, occlusion);

                        float[] faceVertices = FaceGeometry.getFaceVertices(face, worldPos, voxelSize / 2, shadedColor);
                        int[] faceIndices = FaceGeometry.getFaceIndices(vertexOffset);

                        for (float v : faceVertices) vertexData.add(v);
                        for (int i : faceIndices) indexData.add(i);
                        vertexOffset += 4;
                    }
                }
            }
        }

        mesh = new Mesh(toFloatArray(vertexData), toIntArray(indexData));
    }

    public Mesh getMesh() {
        if (mesh == null) buildMesh();
        return mesh;
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

    private int getOcclusion(int x, int y, int z, Face face) {
        return switch (face) {
            case TOP -> isAir(x, y + 1, z) ? 0 : 1;
            case BOTTOM -> isAir(x, y - 1, z) ? 0 : 1;
            case LEFT -> isAir(x - 1, y, z) ? 0 : 1;
            case RIGHT -> isAir(x + 1, y, z) ? 0 : 1;
            case FRONT -> isAir(x, y, z + 1) ? 0 : 1;
            case BACK -> isAir(x, y, z - 1) ? 0 : 1;
        };
    }

    private float[] applyAO(float[] baseColor, int occlusionLevel) {
        float shade = 1.0f - (occlusionLevel * 0.15f);
        return new float[]{
                baseColor[0] * shade,
                baseColor[1] * shade,
                baseColor[2] * shade
        };
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

    public int getHighestBlock(int x, int z) {
        for (int y = CHUNK_HEIGHT - 1; y >= 0; y--) {
            if (blocks[x][y][z] != 0) return y;
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